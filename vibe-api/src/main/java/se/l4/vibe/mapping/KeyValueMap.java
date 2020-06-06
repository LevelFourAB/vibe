package se.l4.vibe.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * {@link KeyValueMappable} that is implemented like a {@link java.util.Map}.
 */
public class KeyValueMap
	implements KeyValueMappable
{
	private final Map<String, Object> values;

	public KeyValueMap()
	{
		this.values = new HashMap<>();
	}

	public void set(String key, Object value)
	{
		values.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<T> get(String key)
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
}
