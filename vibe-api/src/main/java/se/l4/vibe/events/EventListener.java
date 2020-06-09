package se.l4.vibe.events;

/**
 * Listener for events.
 *
 * @param <T>
 */
public interface EventListener<T>
{
	/**
	 * A new event has been registered.
	 *
	 * @param events
	 * @param event
	 */
	void eventRegistered(Events<T> events, EventSeverity severity, T event);
}
