package se.l4.vibe.operations;

@FunctionalInterface
public interface Operation<Input, Output>
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
