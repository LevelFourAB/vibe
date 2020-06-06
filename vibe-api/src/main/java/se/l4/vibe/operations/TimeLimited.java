package se.l4.vibe.operations;

import java.time.Duration;

import se.l4.vibe.internal.sampling.RollingTimeLimitedSampleOperation;
import se.l4.vibe.sampling.SampleListOperation;
import se.l4.vibe.sampling.SampleOperation;

/**
 * Operations that are time limited.
 */
public class TimeLimited
{
	private TimeLimited()
	{
	}

	/**
	 * Create a {@link SampleOperation} that deals with samples within a
	 * certain duration. This operation will be rolling, when a sample drops
	 * out of the
	 * @param <I>
	 * @param <O>
	 * @param duration
	 * @param operation
	 * @return
	 */
	public static <I, O> SampleOperation<I, O> rollingOver(
		Duration duration,
		SampleListOperation<I, O> operation
	)
	{
		return new RollingTimeLimitedSampleOperation<>(duration.toMillis(), operation);
	}
}
