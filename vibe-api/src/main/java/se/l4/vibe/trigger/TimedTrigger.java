package se.l4.vibe.trigger;

import se.l4.vibe.Vibe;

/**
 * Special extension to {@link Trigger} that helps with getting a default
 * repeat time. 
 * 
 * See {@link Vibe.TriggerBuilder#atMostEvery(long, java.util.concurrent.TimeUnit)}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TimedTrigger<Input, Output>
	extends Trigger<Input, Output>
{
	/**
	 * Get the default time to repeat events on.
	 * 
	 * @return
	 */
	long getDefaultRepeatTime();
}
