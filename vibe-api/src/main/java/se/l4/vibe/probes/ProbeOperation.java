package se.l4.vibe.probes;

/**
 * Operation that can be applied to a {@link Probe}.
 *
 * @param <I>
 * @param <O>
 */
public interface ProbeOperation<I, O>
{
	/**
	 * Apply this operation to the given input returning a new value.
	 *
	 * @param input
	 *   the input to modify
	 * @return
	 *   modified result
	 */
	O apply(I input);
}
