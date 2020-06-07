package se.l4.vibe.probes;

import se.l4.vibe.Exportable;
import se.l4.vibe.internal.MergedProbes;
import se.l4.vibe.mapping.KeyValueMap;

/**
 * Probe that can measure a certain value.
 *
 * @param <T>
 */
public interface Probe<T>
	extends Exportable
{
	/**
	 * Read the value.
	 *
	 * @return
	 */
	T read();

	/**
	 * Create a new probe that applies the given operation to the value of this
	 * probe.
	 *
	 * @param <O>
	 * @param operation
	 *   operation to apply
	 * @return
	 *   new probe
	 */
	default <O> Probe<O> apply(ProbeOperation<T, O> operation)
	{
		return () -> {
			T value = this.read();
			return operation.apply(value);
		};
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
		return new MergedProbes.ProbeBuilder();
	}

	/**
	 * Builder for creating a {@link Probe} that contains values from several
	 * other probes.
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
		MergedBuilder add(String name, Probe<?> probe);

		/**
		 * Create the probe.
		 *
		 * @return
		 */
		Probe<KeyValueMap> build();
	}
}
