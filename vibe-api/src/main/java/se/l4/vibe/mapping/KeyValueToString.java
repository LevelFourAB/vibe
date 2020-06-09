package se.l4.vibe.mapping;

/**
 * Utility for creating a {@link #toString()} for something that is a
 * {@link KeyValueMappable}.
 */
public class KeyValueToString
{
	private KeyValueToString()
	{
	}

	public static String toString(KeyValueMappable instance)
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
