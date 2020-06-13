package se.l4.vibe.internal.timer;

import java.util.concurrent.TimeUnit;

import se.l4.vibe.percentiles.PercentileSnapshot;
import se.l4.vibe.snapshots.KeyValueReceiver;
import se.l4.vibe.timers.TimerSnapshot;

/**
 * Implementation of {@link TimerSnapshot}.
 */
public class TimerSnapshotImpl
	implements TimerSnapshot
{
	private final TimeUnit resolution;
	private final PercentileSnapshot snapshot;
	private final long min;
	private final long max;

	public TimerSnapshotImpl(
		TimeUnit resolution,
		PercentileSnapshot snapshot,
		long min,
		long max
	)
	{
		this.resolution = resolution;
		this.snapshot = snapshot;
		this.min = min;
		this.max = max;
	}

	@Override
	public TimeUnit getResolution()
	{
		return resolution;
	}

	@Override
	public long getTotalTime()
	{
		return snapshot.getTotal();
	}

	@Override
	public long getSamples()
	{
		return snapshot.getSamples();
	}

	@Override
	public double getAverage()
	{
		return snapshot.getTotal() / (double) snapshot.getSamples();
	}

	@Override
	public long getMinimum()
	{
		return min;
	}

	@Override
	public long getMaximum()
	{
		return max;
	}

	@Override
	public TimerSnapshot add(TimerSnapshot other)
	{
		return new TimerSnapshotImpl(
			resolution,
			snapshot.add(((TimerSnapshotImpl) other).snapshot),
			Math.min(other.getMinimum(), min),
			Math.min(other.getMaximum(), max)
		);
	}

	@Override
	public TimerSnapshot remove(TimerSnapshot other)
	{
		return new TimerSnapshotImpl(
			resolution,
			snapshot.remove(((TimerSnapshotImpl) other).snapshot),
			Math.min(other.getMinimum(), min),
			Math.min(other.getMaximum(), max)
		);
	}

	@Override
	public void mapToKeyValues(KeyValueReceiver receiver)
	{
		receiver.add("samples", snapshot.getSamples());
		receiver.add("totalTime", snapshot.getTotal());
		receiver.add("average", getAverage());
		receiver.add("min", getMinimum());
		receiver.add("max", getMaximum());

		snapshot.partialMapToKeyValues(receiver);
	}

	@Override
	public String toString()
	{
		return "TimerSnapshot{" +
			"resolution=" + resolution +
			", samples=" + getSamples() +
			", totalTime=" + getTotalTime() +
			", averageTime=" + getAverage() +
		"}";
	}
}
