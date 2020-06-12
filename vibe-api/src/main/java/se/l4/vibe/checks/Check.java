package se.l4.vibe.checks;

import java.time.Duration;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import se.l4.vibe.Exportable;
import se.l4.vibe.Handle;
import se.l4.vibe.Vibe;
import se.l4.vibe.internal.CheckImpl;
import se.l4.vibe.sampling.SampleOperation;
import se.l4.vibe.sampling.Sampler;

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
	Handle addListener(CheckListener listener);

	/**
	 * Remove a listener from the check.
	 *
	 * @param listener
	 *   listener to remove
	 */
	void removeListener(CheckListener listener);

	/**
	 * Start building a new instance.
	 *
	 * @return
	 *   builder for instance
	 */
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
		 * Set the condition for when the check based on the value of
		 * something being sampled.
		 *
		 * @param <I>
		 * @param sampler
		 * @return
		 */
		<I> SamplerWhenBuilder<I> whenSampler(Sampler<I> sampler);

		/**
		 * Set the condition for when to check based on a {@link BooleanSupplier}
		 * that is checked at a certain interval.
		 *
		 * @param supplier
		 * @return
		 */
		BooleanSupplierWhenBuilder whenSupplier(BooleanSupplier supplier);

		/**
		 * Request that the check triggers repeating events when conditions
		 * are met. Can be used to implement things such as sending reminders
		 * for health checks.
		 *
		 * @param duration
		 * @return
		 */
		Builder whenMetRepeatEvery(Duration duration);

		/**
		 * Request that the check triggers repeating events when the the
		 * conditions of the check are not met. Can be used to implement things
		 * such as sending reminders for health checks.
		 *
		 * @param duration
		 * @return
		 */
		Builder whenUnmetRepeatEvery(Duration duration);

		/**
		 * Build the trigger.
		 *
		 * @return
		 */
		Check build();
	}

	/**
	 * Builder for a condition on top of a {@link Sampler}.
	 */
	interface SamplerWhenBuilder<I>
	{
		/**
		 * Apply a {@link SampleOperation operation} to change the value being
		 * checked.
		 *
		 * @param <O>
		 * @param modifier
		 * @return
		 */
		<O> SamplerWhenBuilder<O> apply(SampleOperation<I, O> operation);

		/**
		 * Set the predicate that checks if the value is meets the desired
		 * condition.
		 *
		 * @param condition
		 * @return
		 */
		Builder is(Predicate<I> condition);
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
		BooleanSupplierWhenBuilder setCheckInterval(Duration duration);

		/**
		 * Indicate that the supplier is done and continue building the check.
		 *
		 * @return
		 */
		Builder done();
	}
}
