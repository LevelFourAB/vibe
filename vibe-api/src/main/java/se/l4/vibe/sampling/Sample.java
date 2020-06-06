package se.l4.vibe.sampling;

import se.l4.vibe.internal.sampling.SampleImpl;

/**
 * Sample as acquired by {@link Sampler} from a {@link SampledProbe}.
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
	T getValue();

	/**
	 * Create a new sample for the given time and value.
	 */
	static <T> Sample<T> create(long time, T value)
	{
		return new SampleImpl<T>(time, value);
	}
}
