package se.l4.vibe.event;


/**
 * Abstraction for a collection of events.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface Events<T>
{
	/**
	 * Register a new event.
	 * 
	 * @param event
	 */
	void register(T event);
	
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
	void addListener(EventListener<T> listener);
	
	/**
	 * Remove a listener that will receive events.
	 * 
	 * @param listener
	 */
	void removeListener(EventListener<T> listener);
}
