package se.l4.vibe.percentiles;

import edu.umd.cs.findbugs.annotations.NonNull;
import se.l4.vibe.snapshots.KeyValueReceiver;
import se.l4.vibe.snapshots.Snapshot;

/**
 * Snapshot of the state of a {@link PercentileCounter}.
 */
public interface PercentileSnapshot
	extends Snapshot
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
	@NonNull
	PercentileSnapshot remove(@NonNull PercentileSnapshot other);

	/**
	 * Add the value from the given object and return a new copy.
	 *
	 * @param other
	 * @return
	 */
	@NonNull
	PercentileSnapshot add(@NonNull PercentileSnapshot other);

	@Override
	default void mapToKeyValues(KeyValueReceiver receiver)
	{
		receiver.add("samples", getSamples());
		receiver.add("total", getTotal());

		partialMapToKeyValues(receiver);
	}

	/**
	 *
	 * @param receiver
	 */
	void partialMapToKeyValues(@NonNull KeyValueReceiver receiver);
}
