package se.l4.vibe.trigger;

/**
 * Combination for running a {@link Trigger} on the output of another
 * one.
 * 
 * @author Andreas Holstenson
 *
 * @param <Input>
 * @param <Output>
 */
public interface On<Input, Output>
{
	/**
	 * Build the actual trigger.
	 * 
	 * @param second
	 * @return
	 */
	<T> Trigger<Input, T> build(Trigger<Output, T> second);
}
