package se.l4.vibe;

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
	T get();

	/**
	 * Remove this export.
	 */
	void remove();
}
