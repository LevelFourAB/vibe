package se.l4.vibe.internal.timer;

import se.l4.vibe.mapping.KeyValueMappable;
import se.l4.vibe.mapping.KeyValueReceiver;
import se.l4.vibe.percentile.PercentileSnapshot;
import se.l4.vibe.timer.TimerSnapshot;

/**
 * Implementation of {@link TimerSnapshot}.
 *
 * @author Andreas Holstenson
 *
 */
public class TimerSnapshotImpl
	implements TimerSnapshot, KeyValueMappable
{
	private final PercentileSnapshot snapshot;
	private final long min;
	private final long max;

	public TimerSnapshotImpl(PercentileSnapshot snapshot, long min, long max)
	{
		this.snapshot = snapshot;
		this.min = min;
		this.max = max;
	}

	@Override
	public long getTotalTimeInMs()
	{
		return snapshot.getTotal() / 1000000;
	}

	@Override
	public long getTotalTimeInNs()
	{
		return snapshot.getTotal();
	}

	@Override
	public long getSamples()
	{
		return snapshot.getSamples();
	}

	@Override
	public double getAverageInMs()
	{
		return snapshot.getTotal() / 1000000.0 / snapshot.getSamples();
	}

	@Override
	public double getAverageInNs()
	{
		return snapshot.getTotal() / (double) snapshot.getSamples();
	}

	@Override
	public long getMinimumInNs()
	{
		return min;
	}

	@Override
	public long getMinimumInMs()
	{
		return min / 1000000;
	}

	@Override
	public long getMaximumInNs()
	{
		return max;
	}

	@Override
	public long getMaximumInMs()
	{
		return max / 1000000;
	}

	@Override
	public TimerSnapshot add(TimerSnapshot other)
	{
		return new TimerSnapshotImpl(
			snapshot.add(((TimerSnapshotImpl) other).snapshot),
			Math.min(other.getMinimumInNs(), min),
			Math.min(other.getMaximumInNs(), max)
		);
	}

	@Override
	public TimerSnapshot remove(TimerSnapshot other)
	{
		return new TimerSnapshotImpl(
			snapshot.remove(((TimerSnapshotImpl) other).snapshot),
			Math.min(other.getMinimumInNs(), min),
			Math.min(other.getMaximumInNs(), max)
		);
	}

	@Override
	public void mapToKeyValues(KeyValueReceiver receiver)
	{
		receiver.add("samples", snapshot.getSamples());
		receiver.add("totalTime", snapshot.getTotal() / 1000000);
		receiver.add("average", getAverageInMs());
		receiver.add("min", getMinimumInMs());
		receiver.add("max", getMaximumInMs());
	}

	@Override
	public String toString()
	{
		return "TimerSnapshot{" +
			"samples=" + getSamples() +
			", totalTimeMs=" + getTotalTimeInMs() +
			", averageTimeMs=" + getAverageInMs() +
		"}";
	}
}
