package se.l4.vibe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.backend.VibeBackend;
import se.l4.vibe.event.EventSeverity;
import se.l4.vibe.event.Events;
import se.l4.vibe.internal.EventsImpl;
import se.l4.vibe.internal.SampleTime;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampleListener;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.probes.TimeSeries;
import se.l4.vibe.probes.TimeSeries.Entry;
import se.l4.vibe.probes.TimeSeriesSampler;
import se.l4.vibe.trigger.Condition;
import se.l4.vibe.trigger.On;
import se.l4.vibe.trigger.TimedTrigger;
import se.l4.vibe.trigger.Trigger;
import se.l4.vibe.trigger.TriggerEvent;
import se.l4.vibe.trigger.TriggerListener;

/**
 * Implementation of {@link Vibe}.
 * 
 * @author Andreas Holstenson
 *
 */
public class DefaultVibe
	implements Vibe
{
	private final VibeBackend backend;
	private final long defaultSampleInterval;
	private final long defaultSampleRetention;
	
	private final Map<SampleTime, TimeSeriesSampler> samplers;
	private final ScheduledExecutorService executor;

	/**
	 * Create a new instance.
	 * 
	 * @param backend
	 * 		backend to send all built instances to
	 * @param sampleInterval
	 * 		sampling interval in ms
	 * @param sampleRetention
	 * 		sample retention in ms
	 */
	public DefaultVibe(VibeBackend backend, long sampleInterval, long sampleRetention)
	{
		this.backend = backend;
		this.defaultSampleInterval = sampleInterval;
		this.defaultSampleRetention = sampleRetention;
		
		samplers = new HashMap<SampleTime, TimeSeriesSampler>();
		
		executor = Executors.newScheduledThreadPool(1, new ThreadFactory()
		{
			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread(r, "vibe");
				t.setDaemon(true);
				return t;
			}
		});
	}
	
	/**
	 * Start building a new {@link Vibe}.
	 * 
	 * @return
	 */
	public static VibeBuilder builder()
	{
		return new DefaultVibeBuilder();
	}

	@Override
	public <T> ProbeBuilder<T> probe(Probe<T> probe)
	{
		return new ProbeBuilderImpl<T>(probe);
	}

	@Override
	public <T> TimeSeriesBuilder<T> timeSeries(SampledProbe<T> probe)
	{
		return new TimeSeriesBuilderImpl<T>(probe);
	}
	
	@Override
	public <T> EventsBuilder<T> events(Class<T> base)
	{
		return new EventsBuilderImpl<T>();
	}
	
	private static class AbstractBuilder<Self>
		implements Builder<Self>
	{
		protected String path;
		
		@Override
		public Self at(String path)
		{
			this.path = path;
			
			return (Self) this;
		}
		
		@Override
		public Self at(String... hierarchy)
		{
			StringBuilder path = new StringBuilder();
			for(int i=0, n=hierarchy.length; i<n; i++)
			{
				if(i > 0) path.append('/');
				
				String segment = hierarchy[i];
				if(segment.indexOf('/') != -1)
				{
					throw new IllegalArgumentException("Segments may not contain /; For " + segment);
				}
				
				path.append(segment);
			}
			
			this.path = path.toString();
			
			return (Self) this;
		}
		
		protected void verify()
		{
			if(path == null)
			{
				throw new IllegalStateException("A path is required");
			}
		}
	}
	
	/**
	 * Builder for time series.
	 * 
	 * @author Andreas Holstenson
	 *
	 * @param <T>
	 */
	private class TimeSeriesBuilderImpl<T>
		extends AbstractBuilder<TimeSeriesBuilder<T>>
		implements TimeSeriesBuilder<T>
	{
		private final SampledProbe<T> probe;
		
		private final List<TriggerHolder> triggers;
		private Events<TriggerEvent> triggerEvents;
		
		private long sampleInterval;
		private long sampleRetention;
		
		public TimeSeriesBuilderImpl(SampledProbe<T> probe)
		{
			this.probe = probe;
			
			triggers = new ArrayList<TriggerHolder>();
			
			sampleInterval = defaultSampleInterval;
			sampleRetention = defaultSampleRetention;
		}
		
		@Override
		public TimeSeries<T> export()
		{
			verify();
			
			// Find or create a suitable sampler
			SampleTime time = new SampleTime(sampleInterval, sampleRetention);
			TimeSeriesSampler sampler = samplers.get(time);
			if(sampler == null)
			{
				sampler = new TimeSeriesSampler(executor, sampleInterval, sampleRetention, TimeUnit.MILLISECONDS);
				samplers.put(time, sampler);
				sampler.start();
			}
			
			// Create the series
			TimeSeries<T> series = sampler.add(probe);
			
			// Create the triggers
			List<Runnable> builtTriggers = new ArrayList<Runnable>(); 
			for(TriggerHolder th : triggers)
			{
				Runnable r = th.create(series, triggerEvents);
				builtTriggers.add(r);
			}
			
			backend.export(path, series);
			
			// Create listener for triggers
			final Runnable[] triggers = builtTriggers.toArray(new Runnable[builtTriggers.size()]);
			series.addListener(new SampleListener<T>()
			{
				@Override
				public void sampleAcquired(SampledProbe<T> probe, Entry<T> value)
				{
					for(Runnable r : triggers)
					{
						r.run();
					}
				}
			});
			
			return series;
		}
		
		@Override
		public <Type> TriggerBuilder<TimeSeriesBuilder<T>> when(
				Trigger<? super T, Type> trigger, 
				Condition<Type> condition)
		{
			verify();
			
			if(triggerEvents == null)
			{
				triggerEvents = new EventsImpl<TriggerEvent>(EventSeverity.WARN);
				backend.export(path, triggerEvents);
			}
			
			return new TimeSeriesTriggerBuilder(this, trigger, condition, triggerEvents);
		}
		
		@Override
		public <Type, Middle> TriggerBuilder<TimeSeriesBuilder<T>> when(
				Trigger<Middle, Type> trigger, 
				On<? super T, Middle> on,
				Condition<Type> condition)
		{
			Trigger<? super T, Type> newTrigger = on.build(trigger);
			
			return when(newTrigger, condition);
		}
		
		@Override
		public TimeSeriesBuilder<T> withInterval(long time, TimeUnit unit)
		{
			sampleInterval = unit.toMillis(time);
			return this;
		}
		
		@Override
		public TimeSeriesBuilder<T> withRetention(long time, TimeUnit unit)
		{
			sampleRetention = unit.toMillis(time);
			return this;
		}
	}
	
	private static class TriggerHolder<Input, Output>
	{
		private final Trigger<Input, Output> trigger;
		private final Condition<Output> condition;
		private final long maxEvery;
		private final boolean sendOnNormal;
		private final TriggerListener listener;

		public TriggerHolder(
				Trigger<Input, Output> trigger, 
				Condition<Output> condition,
				long maxEvery,
				boolean sendOnNormal,
				TriggerListener listener)
		{
			this.trigger = trigger;
			this.condition = condition;
			this.maxEvery = maxEvery;
			this.sendOnNormal = sendOnNormal;
			this.listener = listener;
		}

		public Runnable create(final TimeSeries<Input> series, final Events<TriggerEvent> events)
		{
			final Probe<Output> probe = trigger.forTimeSeries(series);
			return new Runnable()
			{
				private long lastEvent;
				
				@Override
				public void run()
				{
					Output value = probe.read();
					if(condition.matches(value))
					{
						long now = System.currentTimeMillis();
						
						if(lastEvent > 0 && maxEvery > 0)
						{
							// Check if we should send an event or not
							long diff = now - lastEvent;
							if(diff < maxEvery)
							{
								// Within the interval
								return;
							}
						}
						
						lastEvent = now;
						String desc = trigger.toString() + " " + condition.toString() + " (value is " + value + ")";
						listener.onEvent(new TriggerEvent(desc, true));
					}
					else if(sendOnNormal && lastEvent > 0)
					{
						String desc = "value is now " + value + ", no longer matching: " + trigger.toString() + " " + condition.toString();
						listener.onEvent(new TriggerEvent(desc, false));
						
						// Reset last event time
						lastEvent = 0;
					}
				}
			};
		}
	}
	
	/**
	 * Exporter for probes.
	 * 
	 * @author Andreas Holstenson
	 *
	 * @param <T>
	 */
	private class ProbeBuilderImpl<T>
		extends AbstractBuilder<ProbeBuilder<T>>
		implements ProbeBuilder<T>
	{
		private final Probe<T> probe;

		public ProbeBuilderImpl(Probe<T> probe)
		{
			this.probe = probe;
		}
		
		@Override
		public Probe<T> export()
		{
			verify();
			
			backend.export(path, probe);
			
			return probe;
		}
	}
	
	private class EventsBuilderImpl<T>
		extends AbstractBuilder<EventsBuilder<T>>
		implements EventsBuilder<T>
	{
		private EventSeverity severity;

		public EventsBuilderImpl()
		{
			severity = EventSeverity.INFO;
		}
		
		@Override
		public EventsBuilder<T> setSeverity(EventSeverity severity)
		{
			this.severity = severity;
			
			return this;
		}
		
		@Override
		public Events<T> create()
		{
			verify();
			
			EventsImpl<T> events = new EventsImpl<T>(severity);
			backend.export(path, events);
			
			return events;
		}
	}
	
	private class TimeSeriesTriggerBuilder<T, Input, Output>
		implements TriggerBuilder<TimeSeriesBuilder<T>>
	{
		private final TimeSeriesBuilderImpl<T> builder;
		private final Trigger<Input, Output> trigger;
		private final Condition<Output> condition;
		
		private final Events<TriggerEvent> events;
		
		private long maxTime;
		private boolean whenNoLongerMet;

		public TimeSeriesTriggerBuilder(
				TimeSeriesBuilderImpl<T> builder,
				Trigger<Input, Output> trigger,
				Condition<Output> condition,
				Events<TriggerEvent> events)
		{
			this.builder = builder;
			this.trigger = trigger;
			this.condition = condition;
			this.events = events;
			
			maxTime = trigger instanceof TimedTrigger
				? ((TimedTrigger) trigger).getDefaultRepeatTime()
				: 0;
		}

		@Override
		public TriggerBuilder<TimeSeriesBuilder<T>> andWhenNoLongerMet()
		{
			whenNoLongerMet = true;
			
			return this;
		}
		
		@Override
		public TriggerBuilder<TimeSeriesBuilder<T>> onlyOnce()
		{
			maxTime = Long.MAX_VALUE;
			
			return this;
		}
		
		@Override
		public TriggerBuilder<TimeSeriesBuilder<T>> atMostEvery(long duration, TimeUnit unit)
		{
			maxTime = unit.toMillis(duration);
			
			return this;
		}
		
		@Override
		public TimeSeriesBuilder<T> sendEvent(EventSeverity severity)
		{
			return handleWith(new EventTriggerListener(events, severity));
		}
		
		@Override
		public TimeSeriesBuilder<T> handleWith(TriggerListener listener)
		{
			builder.triggers.add(new TriggerHolder<Input, Output>(
				trigger, 
				condition, 
				maxTime,
				whenNoLongerMet,
				listener
			));
				
			return builder;
		}
	}
	
	private static class EventTriggerListener
		implements TriggerListener
	{
		private final Events<TriggerEvent> events;
		private final EventSeverity severity;

		public EventTriggerListener(Events<TriggerEvent> events, EventSeverity severity)
		{
			this.events = events;
			this.severity = severity;
		}
		
		@Override
		public void onEvent(TriggerEvent event)
		{
			events.register(severity, event);
		}
	}
}
