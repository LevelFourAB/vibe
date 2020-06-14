package se.l4.vibe.operations;

import java.time.Duration;

import edu.umd.cs.findbugs.annotations.NonNull;
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
	@NonNull
	public static <I, O> TimeSampleOperation<I, O> rollingOver(
		@NonNull Duration duration,
		@NonNull SampleListOperation<I, O> operation
	)
	{
		return () -> new RollingTimeLimitedSampleOperation<>(duration.toMillis(), operation);
	}
}
