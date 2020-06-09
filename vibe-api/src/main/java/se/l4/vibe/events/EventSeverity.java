package se.l4.vibe.events;

/**
 * Different levels of severity for events.
 */
public enum EventSeverity
{
	/**
	 * Event indicates debug information.
	 */
	DEBUG,
	/**
	 * Event indicates general information.
	 */
	INFO,
	/**
	 * Event indicates a system warning.
	 */
	WARN,
	/**
	 * Event indicates a system error.
	 */
	ERROR,
	/**
	 * Event indicates a critical system error.
	 */
	CRITICAL
}
