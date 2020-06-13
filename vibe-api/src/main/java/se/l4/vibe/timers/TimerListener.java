package se.l4.vibe.timers;

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
	void timingComplete(TimerEvent event);
}
