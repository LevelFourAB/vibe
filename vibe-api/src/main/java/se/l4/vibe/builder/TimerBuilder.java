package se.l4.vibe.builder;

import se.l4.vibe.percentile.PercentileCounter;
import se.l4.vibe.timer.Timer;

/**
 * Builder for instances of {@link Timer}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TimerBuilder
	extends Builder<TimerBuilder>
{
	/**
	 * Measure how many timings fall within a predefined set of ranges. Ranges
	 * are given in milliseconds. 
	 * 
	 * Each bucket  represent a range, the first value given in this array is the 
	 * start of the first bucket and the last one is the upper bound for the 
	 * <i>next to last</i> bucket.
	 * 
	 * <p>
	 * Example:
	 * <pre>
	 * withBuckets(0, 100, 400, 500)
	 * </pre>
	 * 
	 * Buckets created:
	 * <ol>
	 *   <li>0-100</li>
	 *   <li>101-400</li>
	 *   <li>401-500</li>
	 *   <li>501-*</li>
	 * </ol>
	 * 
	 * <p>
	 * This will enable percentile calculations for this timer.
	 * 
	 * @param limits
	 * @return
	 */
	TimerBuilder withBuckets(int... limits);
	
	/**
	 * Set the percentile counter to use for this timer. The counter should be setup
	 * to measure things in nanoseconds.
	 * 
	 * @param counter
	 * @return
	 */
	TimerBuilder withPercentiles(PercentileCounter counter);
	
	/**
	 * Build and return the timer without exporting it.
	 * 
	 * @return
	 */
	Timer build();
	
	/**
	 * Export and return the timer.
	 * 
	 * @return
	 */
	Timer export();
}
