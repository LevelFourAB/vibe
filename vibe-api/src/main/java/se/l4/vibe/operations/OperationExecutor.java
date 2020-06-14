package se.l4.vibe.operations;

/**
 * Executor that performs an option on input data. This is the companion to
 * {@link Operation} that performs the actual operation. An executor may keep
 * state as it is intended to be only used by one data source.
 *
 * @param <Input>
 * @param <Output>
 */
@FunctionalInterface
public interface OperationExecutor<Input, Output>
{
	/**
	 * Apply this operation to the given input returning a new value.
	 *
	 * @param input
	 *   the input to modify
	 * @return
	 *   modified result
	 */
	Output apply(Input input);
}
