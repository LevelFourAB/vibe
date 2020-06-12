package se.l4.vibe.snapshots;

/**
 * Indicate that an object is a snapshot. Snapshots are immutable objects that
 * can be returned by probes. Snapshots can always be mapped to primitive key
 * value pairs. This can be used for more complex objects to support sending
 * them to backends.
 */
public interface Snapshot
{
	/**
	 * Map this object to key value pairs.
	 *
	 * @param receiver
	 */
	void mapToKeyValues(KeyValueReceiver receiver);
}
