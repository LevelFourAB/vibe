package se.l4.vibe.internal;

import java.time.Duration;

/**
 * Guard that helps with dealing with repetitions.
 */
public interface RepetitionGuard
{
	/**
	 * Initialize and start the guard for the given timestamp.
	 *
	 * @param time
	 */
	void start(long time);

	/**
	 * Check if the given timestamp should cause the guard to repeat.
	 *
	 * @param time
	 * @return
	 *   if repetition should occur
	 */
	boolean checkIfShouldRepeat(long time);

	/**
	 * Create a guard that does not allow any repetitions.
	 *
	 * @return
	 */
	static RepetitionGuard once()
	{
		return new RepetitionGuard()
		{
			@Override
			public void start(long time)
			{
			}

			@Override
			public boolean checkIfShouldRepeat(long time)
			{
				return false;
			}
		};
	}

	/**
	 * Create a guard that allows repetition after a certain duration.
	 *
	 * @return
	 */
	static RepetitionGuard afterDuration(Duration duration)
	{
		final long delay = duration.toMillis();
		return new RepetitionGuard()
		{
			private long lastEvent;

			@Override
			public void start(long time)
			{
				lastEvent = time;
			}

			@Override
			public boolean checkIfShouldRepeat(long time)
			{
				if(time >= lastEvent + delay)
				{
					lastEvent = time;
					return true;
				}
				else
				{
					return false;
				}
			}
		};
	}
}
