package se.l4.vibe.snapshots;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * {@link Snapshot} that is implemented like a {@link java.util.Map}.
 */
public class MapSnapshot
	implements Snapshot
{
	private final Map<String, Object> values;

	private MapSnapshot(
		Map<String, Object> values
	)
	{
		this.values = values;
	}

	@SuppressWarnings("unchecked")
	@NonNull
	public <T> Optional<T> get(@NonNull String key)
	{
		return Optional.ofNullable((T) values.get(key));
	}

	@Override
	public void mapToKeyValues(KeyValueReceiver receiver)
	{
		for(Map.Entry<String, Object> e : values.entrySet())
		{
			receiver.add(e.getKey(), e.getValue());
		}
	}

	/**
	 * Create a new builder for {@link MapSnapshot}.
	 *
	 * @return
	 */
	@NonNull
	public static Builder builder()
	{
		return new Builder();
	}

	public static class Builder
	{
		private final Map<String, Object> values;

		Builder()
		{
			values = new HashMap<>();
		}

		@NonNull
		public Builder set(@NonNull String key, @NonNull Object value)
		{
			values.put(key, value);
			return this;
		}

		@NonNull
		public MapSnapshot build()
		{
			return new MapSnapshot(values);
		}
	}
}
