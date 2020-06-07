package se.l4.vibe.sampling;

import se.l4.vibe.internal.MergedProbes;
import se.l4.vibe.mapping.KeyValueMap;
import se.l4.vibe.probes.Probe;

/**
 * Probe that measures a value that requires sampling to be read.
 *
 * @param <T>
 */
public interface SampledProbe<T>
{
	/**
	 * Sample the current value and optionally reset the probe.
	 *
	 * @return
	 */
	T sample();

	/**
	 * Create a {@link SampledProbe} by reading values from a {@link Probe}.
	 *
	 * @param probe
	 *   the probe to convert
	 * @return
	 *   converted probe
	 */
	static <T> SampledProbe<T> over(Probe<T> probe)
	{
		return probe::read;
	}

	/**
	 * Start building a new probe that contains merged data.
	 *
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * ThreadPoolExecutor executor = ...;
	 *
     * Probe<KeyValueMap> probe = Probe.merged()
     *  .add("maxSize", executor::getMaximumPoolSize)
     *  .add("size", executor:getPoolSize)
     *  .add("active", executor::getActiveCount)
     *  .build();
	 * </pre>
	 */
	static MergedBuilder merged()
	{
		return new MergedProbes.SampledProbeBuilder();
	}

	/**
	 * Builder for creating a {@link SampledProbe} that contains values from
	 * several other probes.
	 */
	interface MergedBuilder
	{
		/**
		 * Add a probe to that will be used to read out the value associated
		 * with the given key.
		 *
		 * @param name
		 *   the name of the property
		 * @param probe
		 *   probe used to read the property
		 * @return
		 *   builder
		 */
		MergedBuilder add(String name, SampledProbe<?> probe);

		/**
		 * Create the probe.
		 *
		 * @return
		 */
		SampledProbe<KeyValueMap> build();
	}
}