package se.l4.vibe.timers;

public interface Stopwatch
	extends AutoCloseable
{
	/**
	 * Stop timing.
	 */
	void close();
}
