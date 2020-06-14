package se.l4.vibe.operations;

import se.l4.vibe.checks.Check;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.sampling.TimeSampler;

/**
 * Operation that can be applied to data read from different objects. To make
 * operations reusable this acts like a factory for instances of
 * {@link OperationExecutor}. Operations should not keep mutable state, but
 * instead such state should exist in the {@link OperationExecutor}.
 *
 * @param <Input>
 * @param <Output>
 *
 * @see Probe
 * @see SampledProbe
 * @see TimeSampler.Builder
 * @see Check.Builder
 */
@FunctionalInterface
public interface Operation<Input, Output>
{
	/**
	 * Create the operation.
	 */
	OperationExecutor<Input, Output> create();
}
