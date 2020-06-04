package se.l4.vibe.builder;

import se.l4.vibe.event.EventSeverity;
import se.l4.vibe.event.Events;

/**
 * Builder for event registration.
 *
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface EventsBuilder<T>
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
	 * Build the instance without exporting it.
	 *
	 * @return
	 */
	Events<T> build();

	/**
	 * Create and export the events instance.
	 *
	 * @return
	 */
	Events<T> export();
}
