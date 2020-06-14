package se.l4.vibe;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * An object that has been exported by a {@link Vibe} instance.
 *
 * @param <T>
 */
public interface Export<T extends Exportable>
{
	/**
	 * Get the exported object.
	 *
	 * @return
	 */
	@NonNull
	T get();

	/**
	 * Remove this export.
	 */
	void remove();
}
