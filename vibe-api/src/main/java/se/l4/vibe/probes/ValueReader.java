package se.l4.vibe.probes;

/**
 * Reads value from a certain object. This can be used to create probes
 * that use values from a more complex object.
 *
 * @param <Input>
 * @param <Output>
 */
public interface ValueReader<Input, Output>
{
	/**
	 * Read the value from the given object.
	 *
	 * @param object
	 * @return
	 */
	Output read(Input object);

	/**
	 * Get an instance that returns the input value.
	 *
	 * @param <I>
	 * @return
	 */
	static <I> ValueReader<I, I> identity()
	{
		return i -> i;
	}
}
