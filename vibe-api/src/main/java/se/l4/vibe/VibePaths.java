package se.l4.vibe;

/**
 * Helper for paths in Vibe.
 */
public class VibePaths
{
	/**
	 * Separator used for paths within Vibe.
	 */
	public static final char SEPARATOR = '/';

	private VibePaths()
	{
	}

	/**
	 * Merge the given hierarchy into a path. Individual segments of the
	 * hierarchy can not contain the path separator ({@code /}).
	 *
	 * @param hierarchy
	 * @return
	 *   merged path
	 */
	public static String hierarchy(String... hierarchy)
	{
		StringBuilder path = new StringBuilder();
		for(int i=0, n=hierarchy.length; i<n; i++)
		{
			if(path.length() > 0) path.append(SEPARATOR);

			String segment = hierarchy[i];
			if(segment.indexOf(SEPARATOR) != -1)
			{
				throw new IllegalArgumentException("Segments may not contain " + SEPARATOR + "; For " + segment);
			}

			segment = segment.trim();
			path.append(segment);
		}

		return path.toString();
	}

	public static String merge(String current, String toAppend)
	{
		if(toAppend == null)
		{
			return current;
		}

		toAppend = toAppend.trim();
		if(toAppend.isEmpty())
		{
			// Skip empty path segments
			return current;
		}

		if(current == null || current.isEmpty())
		{
			return toAppend;
		}
		else
		{
			return current + SEPARATOR + toAppend;
		}
	}
}
