package se.l4.vibe.snapshots;

/**
 * Utility methods for {@link Snapshot}.
 */
public class Snapshots
{
	private Snapshots()
	{
	}

	/**
	 * Turn the given snapshot into a string value.
	 *
	 * @param instance
	 * @return
	 */
	public static String toString(Snapshot instance)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(instance.getClass().getSimpleName());
		builder.append("{ ");

		instance.mapToKeyValues((k, v) -> {
			builder.append(k).append('=').append(v).append(' ');
		});

		builder.append('}');
		return builder.toString();
	}
}
