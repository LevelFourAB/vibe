package se.l4.vibe.probes;

import java.util.function.Supplier;

import se.l4.vibe.Exportable;
import se.l4.vibe.internal.MergedProbes;
import se.l4.vibe.operations.Operation;
import se.l4.vibe.operations.OperationExecutor;
import se.l4.vibe.snapshots.MapSnapshot;

/**
 * Probe that can measure a certain value. Probes are intended to read a simple
 * value, where calling {@link #read()} several times has no side effects.
 * If the value you trying to measure depends on a previous value a
 * {@link SampledProbe} is more applicable.
 *
 * <p>
 * Probes are {@link FunctionalInterface functional interfaces} and as such
 * can easily be implemented via lambdas:
 *
 * <pre>
 * Probe<Double> randomProbe = () -> ThreadLocalRandom.current().nextDouble();
 * </pre>
 *
 * @param <T>
 *   the type of value being read, supports {@link Boolean}, {@link String},
 *   {@link Number} such as integers, longs, floats and doubles. More complex
 *   objects are supported if they implement {@link se.l4.vibe.snapshots.Snapshot}.
 * @see SampledProbe
 */
@FunctionalInterface
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
	default <O> SampledProbe<O> apply(Operation<T, O> operation)
	{
		return () -> {
			Probe<O> probe = apply(operation.create());
			return probe::read;
		};
	}

	/**
	 * Create a new probe that applies the given operation to the value of this
	 * probe.
	 *
	 * @param <O>
	 * @param executor
	 *   operation to apply
	 * @return
	 *   new probe
	 */
	default <O> Probe<O> apply(OperationExecutor<T, O> executor)
	{
		return () -> {
			T value = this.read();
			return executor.apply(value);
		};
	}

	/**
	 * Turn a {@link Supplier} into a probe.
	 *
	 * @param <O>
	 * @param supplier
	 * @return
	 */
	static <O> Probe<O> over(Supplier<O> supplier)
	{
		return supplier::get;
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
		Probe<MapSnapshot> build();
	}
}
