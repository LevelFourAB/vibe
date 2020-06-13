package se.l4.vibe.sampling;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.Handle;
import se.l4.vibe.internal.sampling.SamplerWithOperation;
import se.l4.vibe.internal.sampling.TimeSamplerImpl;
import se.l4.vibe.probes.Probe;

/**
 * Samples measured over time for a certain {@link SampledProbe}.
 */
public interface TimeSampler<T>
	extends Probe<T>
{
	/**
	 * Get the last sample acquired by this instance.
	 *
	 * @return
	 */
	Sample<T> getLastSample();

	/**
	 * Explicitly start this sampler. This allows samplers to be active even
	 * if there are no listeners attached to them.
	 *
	 * <p>
	 * <pre>
	 * Handle handle = sampler.start();
	 *
	 * // When you're done with the sampler release the handle
	 * handle.release();
	 * </pre>
	 *
	 * @return
	 */
	Handle start();

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
	default <O> TimeSampler<O> apply(SampleOperation<T, O> operation)
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
		return new TimeSamplerImpl.BuilderImpl<>(probe);
	}

	/**
	 * Start building a new sampler on top of the given {@link SampledProbe}.
	 *
	 * @return
	 */
	static <T> Builder<T> forProbe(Probe<T> probe)
	{
		return new TimeSamplerImpl.BuilderImpl<>(SampledProbe.over(probe));
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
		Builder<T> withInterval(long time, TimeUnit unit);

		/**
		 * Set how often the probe should be sampled.
		 *
		 * @param time
		 * @return
		 */
		Builder<T> withInterval(Duration time);

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
		TimeSampler<T> build();
	}
}
