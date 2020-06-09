package se.l4.vibe.percentile;

import se.l4.vibe.probes.ModifiableData;

/**
 * Snapshot of the state of a {@link PercentileCounter}.
 */
public interface PercentileSnapshot
	extends ModifiableData<PercentileSnapshot>
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
}
