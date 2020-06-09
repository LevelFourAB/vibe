package se.l4.vibe.checks;

import java.time.Instant;

/**
 * Event used with {@link Check} to carry information about the check.
 */
public class CheckEvent
{
	private final boolean conditionsMet;
	private final boolean repeating;
	private final Instant firstActive;

	public CheckEvent(
		boolean active,
		boolean repeating,
		Instant firstActive
	)
	{
		this.conditionsMet = active;
		this.repeating = repeating;
		this.firstActive = firstActive;
	}

	/**
	 * Get if the conditions of the check has been met.
	 *
	 * @return
	 */
	public boolean isConditionsMet()
	{
		return conditionsMet;
	}

	/**
	 * Get if this event is repeating one. See {@link Check.Builder} for
	 * how repeating events work.
	 *
	 * @return
	 */
	public boolean isRepeating()
	{
		return repeating;
	}

	/**
	 * Get when the conditions where last changed. If conditions are met this
	 * is the time they became met and if not it is when they were no longer
	 * being met.
	 *
	 * @return
	 */
	public Instant getLastChange()
	{
		return firstActive;
	}
}
