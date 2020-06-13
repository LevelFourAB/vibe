package se.l4.vibe.timers;

import java.util.concurrent.TimeUnit;

import se.l4.vibe.snapshots.Snapshot;

/**
 * A sample of the state of a {@link Timer}.
 */
public interface TimerSnapshot
	extends Snapshot
{
	/**
	 * Get the resolution of this snapshot.
	 *
	 * @return
	 */
	TimeUnit getResolution();

	/**
	 * Get the total time measured.
	 *
	 * @return
	 */
	long getTotalTime();

	/**
	 * Get how many times we have measured.
	 *
	 * @return
	 */
	long getSamples();

	/**
	 * Get the average time.
	 *
	 * @return
	 */
	double getAverage();

	/**
	 * Get the minimum time.
	 *
	 * @return
	 */
	long getMinimum();

	/**
	 * Get the maximum time.
	 *
	 * @return
	 */
	long getMaximum();

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
