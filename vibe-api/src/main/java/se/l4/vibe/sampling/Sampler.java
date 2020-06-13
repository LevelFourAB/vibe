package se.l4.vibe.sampling;

import se.l4.vibe.Handle;

/**
 * Interface used for sampling something. Samplers are used to perform the
 * actual sampling of values of {@link SampledProbe}.
 *
 * @param <T>
 */
@FunctionalInterface
public interface Sampler<T>
	extends Handle
{
	/**
	 * Sample the next value.
	 *
	 * @return
	 */
	T sample();

	@Override
	default void release()
	{
		// Do nothing
	}
}
