package se.l4.vibe.timers;

import java.util.concurrent.TimeUnit;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Event emitted when something has been timed in a {@link Timer}.
 */
public class TimerEvent
{
	private final TimeUnit resolution;
	private final long duration;

	public TimerEvent(
		@NonNull TimeUnit resolution,
		long duration
	)
	{
		this.resolution = resolution;
		this.duration = duration;
	}

	/**
	 * Get the resolution of the duration this event describes.
	 *
	 * @return
	 */
	@NonNull
	public TimeUnit getResolution()
	{
		return resolution;
	}

	/**
	 * Get the duration of the timed action.
	 *
	 * @return
	 */
	public long getDuration()
	{
		return duration;
	}
}
