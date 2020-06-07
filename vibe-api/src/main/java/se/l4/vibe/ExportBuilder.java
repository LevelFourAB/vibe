package se.l4.vibe;

/**
 * Builder used for exporting objects.
 */
public interface ExportBuilder<T extends Metric>
{
	/**
	 * Set the hierarchy where the feature is to be exported.
	 *
	 * @param hierarchy
	 * @return
	 */
	ExportBuilder<T> at(String... hierarchy);

	/**
	 * Set the hierarchy where the feature is to be exported. The hierarchy
	 * uses the separator {@code /}.
	 *
	 * @param path
	 * @return
	 */
	ExportBuilder<T> at(String path);

	/**
	 * Export the object.
	 *
	 * @return
	 */
	Export<T> done();
}
