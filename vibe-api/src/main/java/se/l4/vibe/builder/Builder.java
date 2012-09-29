package se.l4.vibe.builder;

/**
 * Abstract builder, contains common properties for exports.
 * 
 * @author Andreas Holstenson
 *
 * @param <Self>
 */
public interface Builder<Self>
{
	/**
	 * Set the hierarchy where the feature is to be exported.
	 * 
	 * @param hierarchy
	 * @return
	 */
	Self at(String... hierarchy);
	
	/**
	 * Set the hierarchy where the feature is to be exported. The hierarchy
	 * uses the separator {@code /}.
	 * 
	 * @param path
	 * @return
	 */
	Self at(String path);
	
	/**
	 * Set the hierarchy by taking the full class name and replacing dots
	 * with forward slashes.
	 * 
	 * @param type
	 * @return
	 */
	Self at(Class<?> type);
}