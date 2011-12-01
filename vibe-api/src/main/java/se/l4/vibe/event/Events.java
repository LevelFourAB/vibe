package se.l4.vibe.event;

import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampledProbe;


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
	void addListener(EventListener<T> listener);
	
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
}
