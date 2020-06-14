package se.l4.vibe.checks;

import java.time.Duration;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import edu.umd.cs.findbugs.annotations.NonNull;
import se.l4.vibe.Exportable;
import se.l4.vibe.Handle;
import se.l4.vibe.Vibe;
import se.l4.vibe.internal.CheckImpl;
import se.l4.vibe.operations.Operation;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.TimeSampler;

/**
 * Check is an object used to verify if certain conditions are met. Checks
 * are created using a {@link Builder} created via {@link #builder()}.
 *
 * <p>
 * <pre>
 * Sampler<Double> cpuUsage = Sampler.forProbe(JvmProbes.cpuUsage())
 *  .build();
 *
 * Check check = Check.builder()
 *   .whenSampler(cpuUsage)
 *     .apply(Average.averageOver(Duration.ofMinutes(5)))
 *     .is(Conditions.above(0.9))
 *   .build();
 * </pre>
 *
 * <h2>Using</h2>
 *
 * <p>
 * Checks are active if they have {@link #addListener(CheckListener) listeners},
 * if they have been {@link Vibe#export(se.l4.vibe.Exportable) exported} or
 * {@link #start() manually started}.
 *
 * <p>
 * It's possible to verify the conditions of a check by calling {@link #isConditionsMet()}
 * at any time. For events when the conditions change add a listener
 * via {@link #addListener(CheckListener)}.
 *
 * <pre>
 * check.addListener(event -> {
 *   if(event.isConditionsMet()) {
 *     // Conditions are currently met
 *   } else {
 *     // Conditions are not met
 *   }
 * });
 * </pre>
 */
public interface Check
	extends Exportable
{
	/**
	 * Get if this check is currently matching. Use {@link #addListener(CheckListener)}
	 * to listen for changes to this value.
	 *
	 * @return
	 *   {@code true} if conditions are met, {@code false} otherwise
	 */
	boolean isConditionsMet();

	/**
	 * Explicitly start this check. This allows checks to be active even
	 * if there are no listeners attached to them.
	 *
	 * <p>
	 * <pre>
	 * Handle handle = check.start();
	 *
	 * // When you're done with the check release the handle
	 * handle.release();
	 * </pre>
	 *
	 * @return
	 *   handle that can be released when the code that called this method
	 *   no longer needs the check
	 */
	@NonNull
	Handle start();

	/**
	 * Add a listener that will be notified when the status of the check
	 * changes.
	 *
	 * @param listener
	 *   listener to add
	 * @return
	 *   handle that can be used to remove the listener
	 */
	@NonNull
	Handle addListener(@NonNull CheckListener listener);

	/**
	 * Remove a listener from the check.
	 *
	 * @param listener
	 *   listener to remove
	 */
	void removeListener(@NonNull CheckListener listener);

	/**
	 * Start building a new instance.
	 *
	 * @return
	 *   builder for instance
	 */
	@NonNull
	static Builder builder()
	{
		return new CheckImpl.BuilderImpl();
	}

	/**
	 * Builder for instances of {@link Check}.
	 */
	interface Builder
	{
		/**
		 * Set the condition for the check via a {@link TimeSampler}.
		 *
		 * @param <I>
		 * @param sampler
		 * @return
		 */
		@NonNull
		<I> SamplerWhenBuilder<I> whenTimeSampler(@NonNull TimeSampler<I> sampler);

		/**
		 * Set the condition for the check via a {@link Probe} that will be
		 * checked periodically.
		 *
		 * @param <I>
		 * @param probe
		 * @return
		 */
		@NonNull
		<I> ProbeWhenBuilder<I> whenProbe(@NonNull Probe<I> probe);

		/**
		 * Set the condition for the check via a {@link SampledProbe} that will be
		 * checked periodically.
		 *
		 * @param <I>
		 * @param probe
		 * @return
		 */
		@NonNull
		<I> ProbeWhenBuilder<I> whenProbe(@NonNull SampledProbe<I> probe);

		/**
		 * Set the condition for when to check based on a {@link BooleanSupplier}
		 * that is checked at a certain interval.
		 *
		 * @param supplier
		 * @return
		 */
		@NonNull
		BooleanSupplierWhenBuilder whenSupplier(@NonNull BooleanSupplier supplier);

		/**
		 * Request that the check triggers repeating events when conditions
		 * are met. Can be used to implement things such as sending reminders
		 * for health checks.
		 *
		 * @param duration
		 * @return
		 */
		@NonNull
		Builder whenMetRepeatEvery(@NonNull Duration duration);

		/**
		 * Request that the check triggers repeating events when the the
		 * conditions of the check are not met. Can be used to implement things
		 * such as sending reminders for health checks.
		 *
		 * @param duration
		 * @return
		 */
		@NonNull
		Builder whenUnmetRepeatEvery(@NonNull Duration duration);

		/**
		 * Build the trigger.
		 *
		 * @return
		 */
		@NonNull
		Check build();
	}

	/**
	 * Builder for a condition on top of a {@link TimeSampler}.
	 */
	interface SamplerWhenBuilder<I>
	{
		/**
		 * Apply an operation to the value being sampled.
		 *
		 * @param <O>
		 * @param modifier
		 * @return
		 */
		@NonNull
		<O> SamplerWhenBuilder<O> apply(@NonNull Operation<I, O> operation);

		/**
		 * Apply an operation that can optionally resample based on the time
		 * of samples.
		 *
		 * @param <O>
		 * @param modifier
		 * @return
		 */
		@NonNull
		<O> SamplerWhenBuilder<O> applyResampling(@NonNull Operation<Sample<I>, Sample<O>> operation);

		/**
		 * Set the predicate that checks if the value is meets the desired
		 * condition.
		 *
		 * @param condition
		 * @return
		 */
		@NonNull
		Builder is(@NonNull Predicate<I> condition);
	}

	/**
	 * Builder for a condition on top of a {@link TimeSampler}.
	 */
	interface ProbeWhenBuilder<I>
	{
		/**
		 * Set how often the probe should be checked.
		 *
		 * @param time
		 * @param unit
		 * @return
		 */
		@NonNull
		ProbeWhenBuilder<I> withCheckInterval(@NonNull Duration duration);

		/**
		 * Apply an operation to the value being sampled.
		 *
		 * @param <O>
		 * @param modifier
		 * @return
		 */
		@NonNull
		<O> ProbeWhenBuilder<O> apply(@NonNull Operation<I, O> operation);

		/**
		 * Apply an operation that can optionally resample based on the time
		 * of samples.
		 *
		 * @param <O>
		 * @param modifier
		 * @return
		 */
		@NonNull
		<O> ProbeWhenBuilder<O> applyResampling(@NonNull Operation<Sample<I>, Sample<O>> operation);

		/**
		 * Set the predicate that checks if the value is meets the desired
		 * condition.
		 *
		 * @param condition
		 * @return
		 */
		@NonNull
		Builder is(@NonNull Predicate<I> condition);
	}

	/**
	 * Builder for a condition on top of something that returns a boolean.
	 */
	interface BooleanSupplierWhenBuilder
	{
		/**
		 * Set how often the condition should be checked.
		 *
		 * @param time
		 * @param unit
		 * @return
		 */
		@NonNull
		BooleanSupplierWhenBuilder withCheckInterval(@NonNull Duration duration);

		/**
		 * Indicate that the supplier is done and continue building the check.
		 *
		 * @return
		 */
		@NonNull
		Builder done();
	}
}
