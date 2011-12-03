package se.l4.vibe.internal.builder;

import java.util.concurrent.TimeUnit;

import se.l4.vibe.builder.TimeSeriesBuilder;
import se.l4.vibe.builder.TriggerBuilder;
import se.l4.vibe.event.EventSeverity;
import se.l4.vibe.event.Events;
import se.l4.vibe.trigger.Condition;
import se.l4.vibe.trigger.TimedTrigger;
import se.l4.vibe.trigger.Trigger;
import se.l4.vibe.trigger.TriggerEvent;
import se.l4.vibe.trigger.TriggerListener;

/**
 * Actual builder for triggers, stores data in {@link TriggerHolder}.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 * @param <Input>
 * @param <Output>
 */
public class TimeSeriesTriggerBuilder<T, Input, Output>
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