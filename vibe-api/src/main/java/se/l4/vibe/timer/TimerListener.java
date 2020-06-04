package se.l4.vibe.timer;

/**
 * Listener for timers.
 *
 * @author Andreas Holstenson
 *
 */
public interface TimerListener
{
	/**
	 * Called when a timer is started and then stopped.
	 *
	 * @param currentTimeInMs
	 * @param timeInNanoseconds
	 */
	void timerEvent(long currentTimeInMs, long timeInNanoseconds);
}
