package se.l4.vibe.events;

import se.l4.vibe.Exportable;
import se.l4.vibe.Handle;
import se.l4.vibe.internal.EventsImpl;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.SampledProbe;

/**
 * Event registration, used to register events in a system. The events object
 * is created using a {@link Builder} created via {@link #builder()}.
 *
 * <p>
 * <pre>
 * Events<E> events = Events.builder()
 *   .withSeverity(EventSeverity.INFO)
 *   .build();
 * </pre>
 *
 * <h2>Register and listen to events</h2>
 *
 * Events can be registered with {@link #register(EventData)} to be emitted
 * with the default severity. {@link #register(EventSeverity, EventData)} can
 * be used to emit events with a different severity.
 *
 * <p>
 * Use {@link #addListener(EventListener)} to listen for events being emitted.
 *
 * <h2>Probes</h2>
 *
 * Instances contain two probes, one for total events via {@link #getTotalEventsProbe()}
 * and one sampled probe via {@link #getEventsProbe()}. These probes can be
 * {@link se.l4.vibe.Vibe#export(Exportable) exported}, used with a
 * {@link se.l4.vibe.sampling.TimeSampler} or {@link se.l4.vibe.checks.Check}.
 *
 * @param <T>
 */
public interface Events<T extends EventData>
	extends Exportable
{
	/**
	 * Register a new event using the default severity.
	 *
	 * @param eventData
	 *   data of the event
	 */
	void register(T eventData);

	/**
	 * Register a new event with the specified severity.
	 *
	 * @param severity
	 *   severity to use
	 * @param eventData
	 *   data of the event
	 */
	void register(EventSeverity severity, T eventData);

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
	static <T extends EventData> Builder<T> builder()
	{
		return new EventsImpl.BuilderImpl<>();
	}

	/**
	 * Builder for instances of {@link Events}.
	 */
	interface Builder<T extends EventData>
	{
		/**
		 * Set the severity of the events.
		 *
		 * @param severity
		 * @return
		 */
		Builder<T> withSeverity(EventSeverity severity);

		/**
		 * Build the instance.
		 *
		 * @return
		 *   instance of {@link Events}
		 */
		Events<T> build();
	}
}
