package se.l4.vibe.internal.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.backend.VibeBackend;
import se.l4.vibe.builder.SamplerBuilder;
import se.l4.vibe.builder.TriggerBuilder;
import se.l4.vibe.event.EventSeverity;
import se.l4.vibe.event.Events;
import se.l4.vibe.internal.EventsImpl;
import se.l4.vibe.internal.time.TimeSampler;
import se.l4.vibe.probes.SampleListener;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.probes.Sampler;
import se.l4.vibe.probes.Sampler.Entry;
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
public class SamplerBuilderImpl<T>
	extends AbstractBuilder<SamplerBuilder<T>>
	implements SamplerBuilder<T>
{
	private final VibeBackend backend;
	private final TimeSampler sampler;
	private final SampledProbe<T> probe;

	final List<TriggerHolder> triggers;
	private Events<TriggerEvent> triggerEvents;

	private long sampleInterval;

	public SamplerBuilderImpl(VibeBackend backend,
			TimeSampler sampler,
			SampledProbe<T> probe)
	{
		this.backend = backend;
		this.sampler = sampler;
		this.probe = probe;

		triggers = new ArrayList<TriggerHolder>();

		sampleInterval = sampler.getDefaultTime();
	}

	@Override
	public Sampler<T> build()
	{
		// Find or create a suitable sampler
		Sampler<T> instance = sampler.sampleTimeSeries(sampleInterval, probe);

		// Create the triggers
		List<Runnable> builtTriggers = new ArrayList<Runnable>();
		for(TriggerHolder th : triggers)
		{
			Runnable r = th.create(instance, triggerEvents);
			builtTriggers.add(r);
		}

		// Create listener for triggers
		final Runnable[] triggers = builtTriggers.toArray(new Runnable[builtTriggers.size()]);
		instance.addListener(new SampleListener<T>()
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

		return instance;
	}

	@Override
	public Sampler<T> export()
	{
		verify();

		Sampler<T> instance = build();
		backend.export(path, instance);
		return instance;
	}

	@Override
	public <Type> TriggerBuilder<SamplerBuilder<T>> when(
			Trigger<? super T, Type> trigger,
			Condition<Type> condition)
	{
		verify();

		if(triggerEvents == null)
		{
			triggerEvents = new EventsImpl<TriggerEvent>(EventSeverity.WARN);
			backend.export(path + ':' + triggers.size(), triggerEvents);
		}

		return new SamplerTriggerBuilder(this, trigger, condition, triggerEvents);
	}

	@Override
	public <Type, Middle> TriggerBuilder<SamplerBuilder<T>> when(
			Trigger<Middle, Type> trigger,
			On<? super T, Middle> on,
			Condition<Type> condition)
	{
		Trigger<? super T, Type> newTrigger = on.build(trigger);

		return when(newTrigger, condition);
	}

	@Override
	public SamplerBuilder<T> withInterval(long time, TimeUnit unit)
	{
		sampleInterval = unit.toMillis(time);
		return this;
	}
}
