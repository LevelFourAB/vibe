package se.l4.vibe.check;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import se.l4.vibe.Handle;
import se.l4.vibe.internal.CheckImpl;
import se.l4.vibe.sampling.SampleOperation;
import se.l4.vibe.sampling.Sampler;

public interface Check
{
	/**
	 * Get if this check is currently matching.
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
	 * // When you're done with the sampler release the handle
	 * handle.release();
	 * </pre>
	 *
	 * @return
	 */
	Handle start();

	/**
	 * Add a listener that will be notified when the status of the check
	 * changes.
	 *
	 * @param listener
	 * @return
	 */
	Handle addListener(CheckListener listener);

	/**
	 * Remove a listener from the check.
	 *
	 * @param listener
	 */
	void removeListener(CheckListener listener);

	/**
	 * Start building a new check.
	 *
	 * @return
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
		<I> SamplerWhenBuilder<I> forSampler(Sampler<I> sampler);

		/**
		 * Set the condition for when to check based on a {@link BooleanSupplier}
		 * that is checked at a certain interval.
		 *
		 * @param supplier
		 * @return
		 */
		ConditionWhenBuilder forSupplier(BooleanSupplier supplier);

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
		 * Set the condition that must be met for the trigger to execute.
		 *
		 * @param condition
		 * @return
		 */
		Builder is(Condition<I> condition);
	}

	interface ConditionWhenBuilder
	{
		/**
		 * Set how often the condition should be checked.
		 *
		 * @param time
		 * @param unit
		 * @return
		 */
		ConditionWhenBuilder setCheckInterval(long time, TimeUnit unit);

		Builder done();
	}
}
