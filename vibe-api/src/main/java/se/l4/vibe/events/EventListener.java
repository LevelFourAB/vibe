package se.l4.vibe.events;

import edu.umd.cs.findbugs.annotations.NonNull;

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
	void eventRegistered(@NonNull Event<T> event);
}
