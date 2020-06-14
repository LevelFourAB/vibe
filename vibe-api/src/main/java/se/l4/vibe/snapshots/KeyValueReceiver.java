package se.l4.vibe.snapshots;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Receiver of key value pairs.
 */
public interface KeyValueReceiver
{
	/**
	 * Add a key value pair to this receiver. The value should be the object
	 * type of a primitive or {@link String}.
	 *
	 * @param key
	 * @param value
	 */
	void add(@NonNull String key, @NonNull Object value);
}
