package se.l4.vibe.builder;

import java.util.concurrent.TimeUnit;

import se.l4.vibe.event.EventSeverity;
import se.l4.vibe.probes.TimeSeries;
import se.l4.vibe.trigger.Condition;
import se.l4.vibe.trigger.Conditions;
import se.l4.vibe.trigger.On;
import se.l4.vibe.trigger.Trigger;
import se.l4.vibe.trigger.Triggers;

/**
 * Builder for a {@link TimeSeries time series}. 
 * 
 * @author Andreas Holstenson
 *
 */
public interface TimeSeriesBuilder<T>
	extends Builder<TimeSeriesBuilder<T>>
{
	/**
	 * Override the default interval for sampling.
	 * 
	 * @param time
	 * @param unit
	 * @return
	 */
	TimeSeriesBuilder<T> withInterval(long time, TimeUnit unit);
	
	/**
	 * Override the default retention for samples.
	 * 
	 * @param time
	 * @param unit
	 * @return
	 */
	TimeSeriesBuilder<T> withRetention(long time, TimeUnit unit);

	/**
	 * Create a trigger for this time series. Triggers will send events
	 * if the condition is met. Use {@link Triggers} and {@link Conditions}
	 * to create triggers and conditions suitable for usage with this
	 * method.
	 * 
	 * <p>
	 * Example:
	 * <pre>
	 * when(
	 * 	{@link Triggers#average(long, TimeUnit) average(5, TimeUnit.MINUTES)},
	 * 	{@link Conditions#above(Number) above(0.8)
	 * )
	 * .sendEvent({@link EventSeverity#CRITICAL})
	 * </pre>
	 * 
	 * This will create a trigger that activates when the average of the
	 * series over 5 minutes is above {@code 0.8}.
	 * 
	 * @param trigger
	 * @param condition
	 * @return
	 */
	<Type> TriggerBuilder<TimeSeriesBuilder<T>> when(
		Trigger<? super T, Type> trigger,
		Condition<Type> condition
	);
	
	/**
	 * Create a trigger for this time series. See 
	 * {@link #when(Trigger, Condition)}.
	 * 
	 * @param trigger
	 * @param on
	 * @param condition
	 * @return
	 */
	<Type, Middle> TriggerBuilder<TimeSeriesBuilder<T>> when(
		Trigger<Middle, Type> trigger,
		On<? super T, Middle> on,
		Condition<Type> condition
	);
	
	/**
	 * Export and return the time series.
	 *  
	 * @return
	 */
	TimeSeries<T> export();
}