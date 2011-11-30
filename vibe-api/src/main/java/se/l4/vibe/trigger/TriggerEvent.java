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

	public TriggerEvent(String description)
	{
		this.description = description;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	@Override
	public String toString()
	{
		return description;
	}
}
