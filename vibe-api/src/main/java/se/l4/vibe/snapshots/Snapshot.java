package se.l4.vibe.snapshots;

import edu.umd.cs.findbugs.annotations.NonNull;

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
	void mapToKeyValues(@NonNull KeyValueReceiver receiver);
}
