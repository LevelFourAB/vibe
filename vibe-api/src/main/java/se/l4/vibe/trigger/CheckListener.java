package se.l4.vibe.trigger;

/**
 * Listener for status of a {@link Check}.
 */
public interface CheckListener
{
	/**
	 * Status of the check has been updated.
	 *
	 * @param event
	 */
	void checkStatus(CheckEvent event);
}
