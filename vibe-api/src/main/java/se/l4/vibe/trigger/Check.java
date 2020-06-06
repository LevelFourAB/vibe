package se.l4.vibe.trigger;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import se.l4.vibe.ListenerHandle;
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
	 * Add a listener that will be notified when the status of the trigger
	 * changes.
	 *
	 * @param listener
	 * @return
	 */
	ListenerHandle addListener(CheckListener listener);

	/**
	 * Remove a listener from the trigger.
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

	interface Builder
	{
		/**
		 * Set the condition for when the trigger based on the value of
		 * something being sampled.
		 *
		 * @param <I>
		 * @param sampler
		 * @return
		 */
		<I> SamplerWhenBuilder<I> forSampler(Sampler<I> sampler);

		/**
		 * Set the condition for when to trigger based on a {@link BooleanSupplier}
		 * that is checked at a certain interval.
		 *
		 * @param supplier
		 * @return
		 */
		ConditionWhenBuilder forSupplier(BooleanSupplier supplier);

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
