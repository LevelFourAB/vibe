package se.l4.vibe.trigger;

/**
 * Indicates an event that has been automatically triggered.
 *
 * @author Andreas Holstenson
 *
 */
public class TriggerEvent
{
	private final String description;
	private final boolean conditionMet;

	public TriggerEvent(String description, boolean conditionMet)
	{
		this.description = description;
		this.conditionMet = conditionMet;
	}

	/**
	 * Get a textual description of this event.
	 *
	 * @return
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Get if the trigger condition is met or if this is "back to normal"
	 * event.
	 *
	 * @return
	 */
	public boolean isConditionMet()
	{
		return conditionMet;
	}

	@Override
	public String toString()
	{
		if(! conditionMet)
		{
			return "Back to normal: " + description;
		}

		return description;
	}
}
