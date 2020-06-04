package se.l4.vibe.trigger;

/**
 * Action that can be taken when a trigger condition is met.
 *
 * @author Andreas Holstenson
 *
 */
public interface TriggerListener
{
	/**
	 * Handle the specified trigger event.
	 *
	 * @param event
	 */
	void onEvent(TriggerEvent event);
}
