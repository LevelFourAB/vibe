package se.l4.vibe.mapping;

/**
 * Receiver of key value pairs.
 *
 * @author Andreas Holstenson
 *
 */
public interface KeyValueReceiver
{
	/**
	 * Add a key value pair to this receiver. The value should be the object
	 * type of a primtive or {@link String}.
	 *
	 * @param key
	 * @param value
	 */
	void add(String key, Object value);
}
