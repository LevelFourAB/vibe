package se.l4.vibe;

import se.l4.vibe.backend.VibeBackend;

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
	static Handle empty()
	{
		return () -> {};
	}
}