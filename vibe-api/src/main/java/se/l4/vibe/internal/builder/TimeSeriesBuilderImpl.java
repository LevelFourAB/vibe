package se.l4.vibe.internal.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.backend.VibeBackend;
import se.l4.vibe.builder.TimeSeriesBuilder;
import se.l4.vibe.builder.TriggerBuilder;
import se.l4.vibe.event.EventSeverity;
import se.l4.vibe.event.Events;
import se.l4.vibe.internal.EventsImpl;
import se.l4.vibe.internal.SampleTime;
import se.l4.vibe.internal.time.TimeSampler;
import se.l4.vibe.probes.SampleListener;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.probes.TimeSeries;
import se.l4.vibe.probes.TimeSeries.Entry;
import se.l4.vibe.trigger.Condition;
import se.l4.vibe.trigger.On;
import se.l4.vibe.trigger.Trigger;
import se.l4.vibe.trigger.TriggerEvent;

/**
 * Builder for time series.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class TimeSeriesBuilderImpl<T>
	extends AbstractBuilder<TimeSeriesBuilder<T>>
	implements TimeSeriesBuilder<T>
{
	private final VibeBackend backend;
	private final TimeSampler sampler;
	private final SampledProbe<T> probe;
	
	final List<TriggerHolder> triggers;
	private Events<TriggerEvent> triggerEvents;
	
	private long sampleInterval;
	private long sampleRetention;
	
	public TimeSeriesBuilderImpl(VibeBackend backend, 
			TimeSampler sampler,
			SampledProbe<T> probe)
	{
		this.backend = backend;
		this.sampler = sampler;
		this.probe = probe;
		
		triggers = new ArrayList<TriggerHolder>();
		
		SampleTime time = sampler.getDefaultTime();
		sampleInterval = time.getInterval();
		sampleRetention = time.getRetention();
	}
	
	@Override
	public TimeSeries<T> export()
	{
		verify();
		
		// Find or create a suitable sampler
		SampleTime time = new SampleTime(sampleInterval, sampleRetention);
		
		TimeSeries<T> series = sampler.sampleTimeSeries(time, probe);
		
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