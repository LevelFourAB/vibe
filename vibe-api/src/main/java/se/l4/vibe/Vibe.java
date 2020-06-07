package se.l4.vibe;

import se.l4.vibe.backend.VibeBackend;
import se.l4.vibe.internal.VibeImpl;

/**
 * Main interface for statistics and events.
 *
 */
public interface Vibe
{
	/**
	 * Export the given object.
	 *
	 * @param <T>
	 * @param type
	 * @return
	 */
	<T extends Exportable> ExportBuilder<T> export(T object);

	/**
	 * Create a Vibe instance for the given sub hierarchy.
	 *
	 * @param hierarchy
	 * @return
	 */
	Vibe scope(String... hierarchy);

	/**
	 * Destroy this Vibe instance. For the top level instance this will stop
	 * all backends, for scoped instances this will stop collection of any
	 * metrics exported via the scoped instance.
	 */
	void destroy();

	/**
	 * Start building a new instance.
	 *
	 * @return
	 */
	static Builder builder()
	{
		return new VibeImpl.BuilderImpl();
	}

	/**
	 * Builder for instances of {@link Vibe}.
	 */
	interface Builder
	{
		/**
		 * Add a backend to use with the {@link Vibe} instance.
		 *
		 * @param backend
		 * @return
		 */
		Builder withBackend(VibeBackend backend);

		/**
		 * Build the instance.
		 *
		 * @return
		 */
		Vibe build();
	}
}
