package se.l4.vibe.sampling;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.Handle;
import se.l4.vibe.internal.sampling.SamplerImpl;
import se.l4.vibe.internal.sampling.SamplerWithOperation;
import se.l4.vibe.probes.Probe;

/**
 * Samples measured over time for a certain {@link SampledProbe}.
 */
public interface Sampler<T>
	extends Probe<T>
{
	/**
	 * Get the last sample acquired by this instance.
	 *
	 * @return
	 */
	Sample<T> getLastSample();

	/**
	 * Add a listener to this series.
	 *
	 * @param listener
	 */
	Handle addListener(SampleListener<T> listener);

	/**
	 * Remove a listener from this series.
	 *
	 * @param listener
	 */
	void removeListener(SampleListener<T> listener);

	/**
	 * Return a new sampler that uses the given operation to modify the samples
	 * returned by this sampler.
	 *
	 * @param <O>
	 * @param operation
	 *   operation to apply
	 * @return
	 *   new sampler
	 */
	default <O> Sampler<O> apply(SampleOperation<T, O> operation)
	{
		return new SamplerWithOperation<>(this, operation);
	}

	/**
	 * Start building a new sampler on top of the given {@link SampledProbe}.
	 *
	 * @return
	 */
	static <T> Builder<T> forProbe(SampledProbe<T> probe)
	{
		return new SamplerImpl.BuilderImpl<>(probe);
	}

	interface Builder<T>
	{
		/**
		 * Set how often the probe should be sampled.
		 *
		 * @param time
		 * @param unit
		 * @return
		 */
		Builder<T> setInterval(long time, TimeUnit unit);

		/**
		 * Set how often the probe should be sampled.
		 *
		 * @param time
		 * @return
		 */
		Builder<T> setInterval(Duration time);

		/**
		 * Apply the given operation to the probe.
		 *
		 * @param <O>
		 * @param operation
		 * @return
		 */
		<O> Builder<O> apply(SampleOperation<T, O> operation);

		/**
		 * Build the sampler.
		 *
		 * @return
		 */
		Sampler<T> build();
	}
}
