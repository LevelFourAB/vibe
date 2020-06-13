package se.l4.vibe.timers;

import java.util.function.Supplier;

import se.l4.vibe.Exportable;
import se.l4.vibe.Handle;
import se.l4.vibe.internal.timer.TimerImpl;
import se.l4.vibe.percentiles.PercentileCounter;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.SampledProbe;

/**
 * Timer for timing how long things take.
 */
public interface Timer
	extends Exportable
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
	Handle addListener(TimerListener listener);

	/**
	 * Remove a previously added listener.
	 *
	 * @param listener
	 */
	void removeListener(TimerListener listener);

	/**
	 * Get a probe for the all time maximum time measured.
	 *
	 * @return
	 */
	Probe<Long> getMaximumInNSProbe();

	/**
	 * Get a probe for the all time maximum time measured.
	 *
	 * @return
	 */
	Probe<Long> getMinimumInNSProbe();

	/**
	 * Get a probe that samples snapshot information for this probe.
	 *
	 * @return
	 */
	SampledProbe<TimerSnapshot> getSnapshotProbe();

	/**
	 * Start building a new timer.
	 *
	 * @return
	 */
	static Builder builder()
	{
		return new TimerImpl.BuilderImpl();
	}

	/**
	 * Builder for creating a {@link Timer}.
	 */
	interface Builder
	{
		/**
		 * Measure how many timings fall within a predefined set of ranges.
		 * Ranges are given in milliseconds.
		 *
		 * Each bucket  represent a range, the first value given in this array
		 * is the start of the first bucket and the last one is the upper bound
		 * for the <i>next to last</i> bucket.
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
		Builder withBuckets(int... limits);

		/**
		 * Set the percentile counter to use for this timer. The counter should
		 * be setup to measure things in nanoseconds.
		 *
		 * @param counter
		 * @return
		 */
		Builder withPercentiles(Supplier<PercentileCounter> counter);

		/**
		 * Build the timer.
		 * @return
		 */
		Timer build();
	}
}
