package se.l4.vibe.timer;

import se.l4.vibe.probes.SampledProbe;

/**
 * Timer for timing how long things take. Supports 
 * 
 * @author Andreas Holstenson
 *
 */
public interface Timer
	extends SampledProbe<TimerSnapshot>
{
	/**
	 * Start timing something.
	 */
	Stopwatch start();

	/**
	 * Add a listener that is triggered whenever this timer is stopped.
	 * 
	 * @param listener
	 */
	void addListener(TimerListener listener);
	
	/**
	 * Remove a previously added listener.
	 * 
	 * @param listener
	 */
	void removeListener(TimerListener listener);
}
