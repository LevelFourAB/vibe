package se.l4.vibe.probes;

/**
 * Reads value from a certain object. This can be used to create probes
 * that use values from a more complex object.
 *
 * @author Andreas Holstenson
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
}
