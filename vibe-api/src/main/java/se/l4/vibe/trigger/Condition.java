package se.l4.vibe.trigger;

/**
 * Condition used together with {@link Triggers triggers}. Usually created
 * via {@link Conditions}.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
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
