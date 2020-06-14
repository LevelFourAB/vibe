package se.l4.vibe.sampling;

import edu.umd.cs.findbugs.annotations.NonNull;
import se.l4.vibe.internal.sampling.SampleImpl;
import se.l4.vibe.probes.SampledProbe;

/**
 * Sample as acquired by {@link TimeSampler} from a {@link SampledProbe}.
 */
public interface Sample<T>
{
	/**
	 * Get the time this was sampled.
	 *
	 * @return
	 */
	long getTime();

	/**
	 * Get the value of the sample.
	 *
	 * @return
	 */
	@NonNull
	T getValue();

	/**
	 * Create a new sample for the given time and value.
	 */
	@NonNull
	static <T> Sample<T> create(long time, @NonNull T value)
	{
		return new SampleImpl<T>(time, value);
	}
}
