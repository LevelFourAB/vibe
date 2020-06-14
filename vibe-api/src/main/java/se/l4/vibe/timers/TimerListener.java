package se.l4.vibe.timers;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Listener for timers.
 */
public interface TimerListener
{
	/**
	 * Something has completed timing.
	 *
	 * @param event
	 */
	void timingComplete(@NonNull TimerEvent event);
}
