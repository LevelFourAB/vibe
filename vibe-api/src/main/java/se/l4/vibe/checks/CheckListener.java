package se.l4.vibe.checks;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Listener for status of a {@link Check}.
 *
 * @see Check
 */
public interface CheckListener
{
	/**
	 * Status of the check has been updated.
	 *
	 * @param event
	 */
	void checkStatus(@NonNull CheckEvent event);
}
