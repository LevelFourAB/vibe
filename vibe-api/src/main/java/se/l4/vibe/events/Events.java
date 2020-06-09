package se.l4.vibe.events;

import se.l4.vibe.Exportable;
import se.l4.vibe.Handle;
import se.l4.vibe.internal.EventsImpl;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.SampledProbe;


/**
 * Event registration, used to register events in a system.
 *
 * @param <T>
 */
public interface Events<T>
	extends Exportable
{
	/**
	 * Register a new event.
	 *
	 * @param event
	 */
	void register(T event);

	/**
	 * Register a new event with the specified severity.
	 *
	 * @param severity
	 * @param event
	 */
	void register(EventSeverity severity, T event);

	/**
	 * Get the default severity for this object.
	 *
	 * @return
	 */
	EventSeverity getDefaultSeverity();

	/**
	 * Add a listener that will receive events.
	 *
	 * @param listener
	 */
	Handle addListener(EventListener<T> listener);

	/**
	 * Remove a listener that will receive events.
	 *
	 * @param listener
	 */
	void removeListener(EventListener<T> listener);

	/**
	 * Get a probe that will return the total amount of events registered.
	 *
	 * @return
	 */
	Probe<Long> getTotalEventsProbe();

	/**
	 * Get a probe that will return the number of events received since it
	 * was last sampled.
	 *
	 * @return
	 */
	SampledProbe<Long> getEventsProbe();

	/**
	 * Start building a new {@link Events}.
	 *
	 * @param <T>
	 * @return
	 */
	static <T> Builder<T> builder()
	{
		return new EventsImpl.BuilderImpl<>();
	}

	/**
	 * Builder for instances of {@link Events}.
	 */
	interface Builder<T>
	{
		/**
		 * Set the severity of the events.
		 *
		 * @param severity
		 * @return
		 */
		Builder<T> setSeverity(EventSeverity severity);

		/**
		 * Build the instance.
		 *
		 * @return
		 *   instance of {@link Events}
		 */
		Events<T> build();
	}
}
