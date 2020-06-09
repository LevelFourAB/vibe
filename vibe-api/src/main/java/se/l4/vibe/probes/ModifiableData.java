package se.l4.vibe.probes;

/**
 * Data that can be modified, such as removing or adding values.
 */
public interface ModifiableData<Self extends ModifiableData<Self>>
{
	/**
	 * Remove the values from the given object and return a new copy.
	 *
	 * @param other
	 * @return
	 */
	Self remove(Self other);

	/**
	 * Add the value from the given object and return a new copy.
	 *
	 * @param other
	 * @return
	 */
	Self add(Self other);
}
