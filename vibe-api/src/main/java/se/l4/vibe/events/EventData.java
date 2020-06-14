package se.l4.vibe.events;

import edu.umd.cs.findbugs.annotations.NonNull;

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
	@NonNull
	String toHumanReadable();
}
