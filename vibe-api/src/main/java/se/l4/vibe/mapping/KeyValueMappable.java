package se.l4.vibe.mapping;

/**
 * Indicate that an object can be mapped to primitive key value pairs. This
 * can be used for more complex objects to support sending them to backends.
 */
public interface KeyValueMappable
{
	/**
	 * Map this object to key value pairs.
	 *
	 * @param receiver
	 */
	void mapToKeyValues(KeyValueReceiver receiver);
}
