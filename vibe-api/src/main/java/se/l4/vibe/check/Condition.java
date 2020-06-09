package se.l4.vibe.check;

/**
 * Condition used together with {@link Triggers triggers}. Usually created
 * via {@link Conditions}.
 *
 * @param <T>
 */
@FunctionalInterface
public interface Condition<T>
{
	/**
	 * Check if the condition matches.
	 *
	 * @param value
	 * @return
	 */
	boolean matches(T value);
}
