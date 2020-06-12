package se.l4.vibe.events;

/**
 * Data for an event. Used as a marker interface to make it clear that a class
 * can be used together with {@link Events}.
 */
public interface EventData
{
	/**
	 * Turn this event into a human readable string.
	 *
	 * @return
	 */
	String toHumanReadable();
}
