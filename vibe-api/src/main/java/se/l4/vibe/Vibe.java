package se.l4.vibe;

import java.util.concurrent.TimeUnit;

import se.l4.vibe.event.EventSeverity;
import se.l4.vibe.event.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.probes.TimeSeries;
import se.l4.vibe.trigger.Condition;
import se.l4.vibe.trigger.Conditions;
import se.l4.vibe.trigger.Trigger;
import se.l4.vibe.trigger.Triggers;

/**
 * Main interface for statistics and events.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Vibe
{
	/**
	 * Export a new probe.
	 * 
	 * @param probe
	 * @return
	 */
	<T> ProbeBuilder<T> probe(Probe<T> probe);

	/**
	 * Start creating a new time series.
	 * 
	 * @return
	 */
	<T> TimeSeriesBuilder<T> timeSeries(SampledProbe<T> probe);
	
	/**
	 * Create a new events instance.
	 * 
	 * @param base
	 * @return
	 */
	<T> EventsBuilder<T> events(Class<T> base);

	/**
	 * Abstract builder, contains common properties for exports.
	 * 
	 * @author Andreas Holstenson
	 *
	 * @param <Self>
	 */
	interface Builder<Self>
	{
		/**
		 * Set the hierarchy where the feature is to be exported.
		 * 
		 * @param hierarchy
		 * @return
		 */
		Self at(String... hierarchy);
		
		/**
		 * Set the hierarchy where the feature is to be exported. The hierarchy
		 * uses the separator {@code /}.
		 * 
		 * @param path
		 * @return
		 */
		Self at(String path);
	}

	/**
	 * Builder for simple probes.
	 * 
	 * @author Andreas Holstenson
	 *
	 * @param <T>
	 */
	interface ProbeBuilder<T> extends Builder<ProbeBuilder<T>>
	{
		/**
		 * Export and return the probe.
		 * 
		 * @return
		 */
		Probe<T> export();
	}

	/**
	 * Builder for a {@link TimeSeries time series}. 
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	interface TimeSeriesBuilder<T>
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
		 * trigger(
		 * 	{@link EventSeverity#CRITICAL}, 
		 * 	{@link Triggers#average(long, TimeUnit) average(5, TimeUnit.MINUTES)},
		 * 	{@link Conditions#above(Number) above(0.8)
		 * )
		 * </pre>
		 * 
		 * This will create a trigger that activates when the average of the
		 * series over 5 minutes is above {@code 0.8}.
		 * 
		 * @param severity
		 * @param trigger
		 * @param condition
		 * @return
		 */
		<Type> TimeSeriesBuilder<T> trigger(
			EventSeverity severity, 
			Trigger<? super T, Type> trigger,
			Condition<Type> condition
		);
		
		/**
		 * Export and return the time series.
		 *  
		 * @return
		 */
		TimeSeries<T> export();
	}
	
	/**
	 * Builder for event registration.
	 * 
	 * @author Andreas Holstenson
	 *
	 * @param <T>
	 */
	interface EventsBuilder<T>
		extends Builder<EventsBuilder<T>>
	{
		/**
		 * Set the severity of these events.
		 * 
		 * @param severity
		 * @return
		 */
		EventsBuilder<T> setSeverity(EventSeverity severity);
		
		/**
		 * Create the events instance.
		 * 
		 * @return
		 */
		Events<T> create();
	}
}
