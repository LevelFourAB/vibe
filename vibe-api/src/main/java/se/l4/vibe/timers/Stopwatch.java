package se.l4.vibe.timers;

/**
 * Interface representing a stopwatch acquired from a {@link Timer}.
 *
 * <p>
 * Example:
 *
 * <pre>
 * try(Stopwatch v = timer.start()) {
 *   // Do things that should be timed here
 * }
 * </pre>
 */
public interface Stopwatch
	extends AutoCloseable
{
	/**
	 * Stop timing.
	 */
	void close();
}
