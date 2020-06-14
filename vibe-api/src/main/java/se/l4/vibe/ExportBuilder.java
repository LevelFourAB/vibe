package se.l4.vibe;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Builder used for exporting objects.
 */
public interface ExportBuilder<T extends Exportable>
{
	/**
	 * Set the hierarchy where the feature is to be exported. A path will be
	 * generated using the separator {@code /}.
	 *
	 * @param hierarchy
	 *   the hierarchy to use
	 * @return
	 *   self
	 */
	@NonNull
	ExportBuilder<T> at(@NonNull String... hierarchy);

	/**
	 * Set the hierarchy where the feature is to be exported. The hierarchy
	 * uses the separator {@code /}.
	 *
	 * @param path
	 *   path to export at
	 * @return
	 *   self
	 */
	@NonNull
	ExportBuilder<T> at(@NonNull String path);

	/**
	 * Export the object and return an instance describing the export. The
	 * returned object can be used to access the exported object or remove the
	 * export later.
	 *
	 * @return
	 *   instance describing the export
	 */
	@NonNull
	Export<T> done();
}
