package se.l4.vibe.timers;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import edu.umd.cs.findbugs.annotations.NonNull;
import se.l4.vibe.Exportable;
import se.l4.vibe.Handle;
import se.l4.vibe.internal.timer.TimerImpl;
import se.l4.vibe.percentiles.BucketPercentileCounter;
import se.l4.vibe.percentiles.PercentileCounter;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampledProbe;

/**
 * Timer for timing how long things take.
 */
public interface Timer
	extends Exportable
{
	/**
	 * Start timing something.
	 */
	@NonNull
	Stopwatch start();

	/**
	 * Add a listener that is triggered whenever this timer is stopped.
	 *
	 * @param listener
	 */
	@NonNull
	Handle addListener(@NonNull TimerListener listener);

	/**
	 * Remove a previously added listener.
	 *
	 * @param listener
	 */
	void removeListener(@NonNull TimerListener listener);

	/**
	 * Get the resolution of this timer.
	 *
	 * @return
	 */
	@NonNull
	TimeUnit getResolution();

	/**
	 * Get a probe for the all time maximum time measured.
	 *
	 * @return
	 */
	@NonNull
	Probe<Long> getMaximumProbe();

	/**
	 * Get a probe for the all time maximum time measured.
	 *
	 * @return
	 */
	@NonNull
	Probe<Long> getMinimumProbe();

	/**
	 * Get a probe that samples snapshot information for this probe.
	 *
	 * @return
	 */
	@NonNull
	SampledProbe<TimerSnapshot> getSnapshotProbe();

	/**
	 * Start building a new timer.
	 *
	 * @return
	 */
	@NonNull
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
		 * Set the time resolution for things being timed.
		 *
		 * @param unit
		 *   the unit to use for resolution
		 * @return
		 */
		@NonNull
		Builder withResolution(@NonNull TimeUnit unit);

		/**
		 * Measure how many timings fall within a predefined set of ranges.
		 * This works a bit like a histogram and can be used to estimate
		 * percentiles on snapshots returned via {@link Timer#getSnapshotProbe()}.
		 *
		 * <p>
		 * Each bucket represent a range, the first value given in this array
		 * is the start of the first bucket and the last one is the upper bound
		 * for the <i>next to last</i> bucket.
		 *
		 * <p>
		 * Example:
		 * <pre>
		 * withBuckets(
		 *   Duration.ofMillis(0),
		 *   Duration.ofMillis(100),
		 *   Duration.ofMillis(400),
		 *   Duration.ofMillis(500),
		 *   Duration.ofSeconds(1),
		 *   Duration.ofSeconds(10)
		 * )
		 * </pre>
		 *
		 * <p>
		 * Buckets created if resolution is in milliseconds:
		 *
		 * <ol>
		 *   <li>0-100</li>
		 *   <li>101-400</li>
		 *   <li>401-500</li>
		 *   <li>501-1000</li>
		 *   <li>1001-10000</li>
		 *   <li>10001-*</li>
		 * </ol>
		 *
		 * <p>
		 * This will enable percentile calculations via {@link BucketPercentileCounter}
		 * and automatically scale the buckets based on the limits relative to
		 * {@link #withResolution(TimeUnit) the timer resolution}.
		 *
		 * @param limits
		 * @return
		 * @see BucketPercentileCounter
		 */
		@NonNull
		Builder withBuckets(@NonNull Duration... limits);

		/**
		 * Set the percentile counter to use for this timer. The counter should
		 * be setup to measure things in nanoseconds.
		 *
		 * @param counter
		 * @return
		 */
		@NonNull
		Builder withPercentiles(@NonNull Supplier<PercentileCounter> counter);

		/**
		 * Build the timer.
		 *
		 * @return
		 */
		@NonNull
		Timer build();
	}
}
