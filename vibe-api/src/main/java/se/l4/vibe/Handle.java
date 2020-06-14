package se.l4.vibe;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Handle representing something that can be released, such as a listener or
 * something exported by a {@link VibeBackend backend}.
 */
@FunctionalInterface
public interface Handle
{
	/**
	 * Release the handler.
	 */
	void release();

	/**
	 * Get an empty handle.
	 *
	 * @return
	 */
	@NonNull
	static Handle empty()
	{
		return () -> {};
	}
}
