package se.l4.vibe.percentile;

/**
 * Counter for helping with calculating percentiles.
 * 
 * @author Andreas Holstenson
 *
 */
public interface PercentileCounter
{
	/**
	 * Add a value to this counter.
	 * 
	 * @param value
	 */
	void add(long value);
	
	/**
	 * Get a snapshot of the counter.
	 * 
	 * @return
	 */
	PercentileSnapshot get();
	
	/**
	 * Reset the counter.
	 * 
	 */
	void reset();
}
