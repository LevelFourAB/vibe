package se.l4.vibe.probes;

import edu.umd.cs.findbugs.annotations.NonNull;
import se.l4.vibe.Exportable;
import se.l4.vibe.internal.MergedProbes;
import se.l4.vibe.operations.Operation;
import se.l4.vibe.operations.OperationExecutor;
import se.l4.vibe.sampling.Sampler;
import se.l4.vibe.snapshots.MapSnapshot;

/**
 * Probe that measures a value that requires sampling to be read.
 *
 * @param <T>
 *   the type of value being sampled, supports {@link Boolean}, {@link String},
 *   {@link Number} such as integers, longs, floats and doubles. More complex
 *   objects are supported if they implement {@link se.l4.vibe.snapshots.Snapshot}.
 */
@FunctionalInterface
public interface SampledProbe<T>
	extends Exportable
{
	/**
	 * Create a {@link Sampler} that can be used to sample values on this for
	 * this probe.
	 *
	 * @return
	 */
	@NonNull
	Sampler<T> create();

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
	@NonNull
	default <O> SampledProbe<O> apply(@NonNull Operation<T, O> operation)
	{
		return () -> {
			Sampler<T> probe = this.create();
			OperationExecutor<T, O> executor = operation.create();
			return () -> {
				T value = probe.sample();
				return executor.apply(value);
			};
		};
	}

	/**
	 * Apply the given operation to the probe.
	 *
	 * @param <O>
	 * @param operation
	 * @return
	 */
	@NonNull
	default <O> SampledProbe<O> apply(@NonNull OperationExecutor<T, O> executor)
	{
		return () -> {
			Sampler<T> probe = this.create();
			return () -> {
				T value = probe.sample();
				return executor.apply(value);
			};
		};
	}

	/**
	 * Create a {@link SampledProbe} by reading values from a {@link Probe}.
	 *
	 * @param probe
	 *   the probe to convert
	 * @return
	 *   converted probe
	 */
	@NonNull
	static <T> SampledProbe<T> over(@NonNull Probe<T> probe)
	{
		return () -> probe::read;
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
	@NonNull
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
		@NonNull
		MergedBuilder add(@NonNull String name, @NonNull SampledProbe<?> probe);

		/**
		 * Create the probe.
		 *
		 * @return
		 */
		@NonNull
		SampledProbe<MapSnapshot> build();
	}
}
