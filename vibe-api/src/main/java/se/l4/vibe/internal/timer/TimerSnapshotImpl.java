package se.l4.vibe.internal.timer;

import se.l4.vibe.percentile.PercentileSnapshot;
import se.l4.vibe.timer.TimerSnapshot;

/**
 * Implementation of {@link TimerSnapshot}.
 * 
 * @author Andreas Holstenson
 *
 */
public class TimerSnapshotImpl
	implements TimerSnapshot
{
	private final PercentileSnapshot snapshot;

	public TimerSnapshotImpl(PercentileSnapshot snapshot)
	{
		this.snapshot = snapshot;
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
	public TimerSnapshot add(TimerSnapshot other)
	{
		return new TimerSnapshotImpl(snapshot.add(((TimerSnapshotImpl) other).snapshot));
	}
	
	@Override
	public TimerSnapshot remove(TimerSnapshot other)
	{
		return new TimerSnapshotImpl(snapshot.remove(((TimerSnapshotImpl) other).snapshot));
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
