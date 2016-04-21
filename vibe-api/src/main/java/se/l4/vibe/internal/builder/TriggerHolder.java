package se.l4.vibe.internal.builder;

import se.l4.vibe.event.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.Sampler;
import se.l4.vibe.trigger.Condition;
import se.l4.vibe.trigger.Trigger;
import se.l4.vibe.trigger.TriggerEvent;
import se.l4.vibe.trigger.TriggerListener;

/**
 * Holds information about a trigger on a time series.
 * 
 * @author Andreas Holstenson
 *
 * @param <Input>
 * @param <Output>
 */
public class TriggerHolder<Input, Output>
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

	public Runnable create(final Sampler<Input> series, final Events<TriggerEvent> events)
	{
		final Probe<Output> probe = trigger.forSampler(series);
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