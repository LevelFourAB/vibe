package se.l4.vibe.timers;

/**
 * A sample of the state of a {@link Timer}.
 */
public interface TimerSnapshot
{
	/**
	 * Get the total time measured as milliseconds.
	 *
	 * @return
	 */
	long getTotalTimeInMs();

	/**
	 * Get the total time measured as nanoseconds.
	 *
	 * @return
	 */
	long getTotalTimeInNs();

	/**
	 * Get how many times we have measured.
	 *
	 * @return
	 */
	long getSamples();

	/**
	 * Get the average time in milliseconds.
	 *
	 * @return
	 */
	double getAverageInMs();

	/**
	 * Get the average time in nanoseconds.
	 *
	 * @return
	 */
	double getAverageInNs();

	/**
	 * Get the minimum time in nanoseconds.
	 *
	 * @return
	 */
	long getMinimumInNs();

	/**
	 * Get the minimum time in milliseconds.
	 *
	 * @return
	 */
	long getMinimumInMs();

	/**
	 * Get the maximum time in nanoseconds.
	 *
	 * @return
	 */
	long getMaximumInNs();

	/**
	 * Get the maximum time in milliseconds.
	 *
	 * @return
	 */
	long getMaximumInMs();

	/**
	 * Remove the values from the given object and return a new copy.
	 *
	 * @param other
	 * @return
	 */
	TimerSnapshot remove(TimerSnapshot other);

	/**
	 * Add the value from the given object and return a new copy.
	 *
	 * @param other
	 * @return
	 */
	TimerSnapshot add(TimerSnapshot other);
}
