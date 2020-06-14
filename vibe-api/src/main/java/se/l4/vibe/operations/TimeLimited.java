package se.l4.vibe.operations;

import java.time.Duration;

import se.l4.vibe.internal.sampling.RollingTimeLimitedSampleOperation;
import se.l4.vibe.sampling.SampleListOperation;

/**
 * Operations that are time limited.
 */
public class TimeLimited
{
	private TimeLimited()
	{
	}

	/**
	 * Create an operation that deals with samples within a certain duration.
	 *
	 * @param <I>
	 * @param <O>
	 * @param duration
	 * @param operation
	 * @return
	 */
	public static <I, O> TimeSampleOperation<I, O> rollingOver(
		Duration duration,
		SampleListOperation<I, O> operation
	)
	{
		return () -> new RollingTimeLimitedSampleOperation<>(duration.toMillis(), operation);
	}
}
