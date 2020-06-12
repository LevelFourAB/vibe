package se.l4.vibe.events;

/**
 * Listener for events.
 *
 * @param <T>
 * @see Events
 */
public interface EventListener<T extends EventData>
{
	/**
	 * A new event has been registered.
	 *
	 * @param event
	 */
	void eventRegistered(Event<T> event);
}
