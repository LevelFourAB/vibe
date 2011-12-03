package se.l4.vibe.builder;

import java.util.concurrent.TimeUnit;

import se.l4.vibe.event.EventSeverity;
import se.l4.vibe.trigger.TriggerListener;

/**
 * Builder for triggers.
 * 
 * @author Andreas Holstenson
 *
 * @param <B>
 */
public interface TriggerBuilder<B>
{
	/**
	 * Set that the trigger should also handle the case where the condition
	 * is no longer met.
	 * 
	 * @return
	 */
	TriggerBuilder<B> andWhenNoLongerMet();
	
	/**
	 * Set that the trigger should only trigger once until it is no longer
	 * met. This equivalent of calling {@link #atMostEvery(long, TimeUnit)}
	 * with a high value.
	 * 
	 * @return
	 */
	TriggerBuilder<B> onlyOnce();
	
	/**
	 * Limit the number of times the event is triggered. This time is
	 * normally fetched from the used trigger, but sometimes it may be
	 * desirable to override this with a custom value.
	 * 
	 * @param duration
	 * @param unit
	 * @return
	 */
	TriggerBuilder<B> atMostEvery(long duration, TimeUnit unit);
	
	/**
	 * Indicate that an event should be sent.
	 * 
	 * @param severity
	 * @return
	 */
	B sendEvent(EventSeverity severity);
	
	/**
	 * Handle the trigger with the specified listener.
	 * 
	 * @param listener
	 * @return
	 */
	B handleWith(TriggerListener listener);
}