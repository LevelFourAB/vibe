package se.l4.vibe.percentiles;

/**
 * Snapshot of the state of a {@link PercentileCounter}.
 */
public interface PercentileSnapshot
{
	/**
	 * Get the total value measured.
	 *
	 * @return
	 */
	long getTotal();

	/**
	 * Get how many times we have measured.
	 *
	 * @return
	 */
	long getSamples();

	/**
	 * Estimate the value of the given percentile. This gives the
	 * maximum upper bound if the {@link PercentileCounter} uses
	 * some form of ranges.
	 *
	 * @param percentile
	 * @return
	 */
	long estimatePercentile(int percentile);

	/**
	 * Remove the values from the given object and return a new copy.
	 *
	 * @param other
	 * @return
	 */
	PercentileSnapshot remove(PercentileSnapshot other);

	/**
	 * Add the value from the given object and return a new copy.
	 *
	 * @param other
	 * @return
	 */
	PercentileSnapshot add(PercentileSnapshot other);
}
