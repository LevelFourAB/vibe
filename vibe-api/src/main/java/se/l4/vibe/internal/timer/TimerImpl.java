package se.l4.vibe.internal.timer;

import java.util.concurrent.atomic.AtomicLong;

import se.l4.vibe.percentile.PercentileCounter;
import se.l4.vibe.timer.Stopwatch;
import se.l4.vibe.timer.Timer;
import se.l4.vibe.timer.TimerSnapshot;

/**
 * Implementation of {@link Timer}.
 * 
 * @author Andreas Holstenson
 *
 */
public class TimerImpl
	implements Timer
{
	private final PercentileCounter counter;
	private volatile TimerSnapshot lastSample;
	
	public TimerImpl(PercentileCounter counter)
	{
		this.counter = counter;
	}
	
	private long time()
	{
		return System.nanoTime();
	}
	
	@Override
	public Stopwatch start()
	{
		final long time = time();
		return new Stopwatch()
		{
			@Override
			public void stop()
			{
				long now = time();
				
				counter.add(now - time);
			}
		};
	}
	
	private TimerSnapshot createSample()
	{
		return new TimerSnapshotImpl(counter.get());
	}

	@Override
	public TimerSnapshot peek()
	{
		return createSample();
	}
	
	@Override
	public TimerSnapshot read()
	{
		return lastSample;
	}
	
	@Override
	public TimerSnapshot sample()
	{
		lastSample = createSample();
		counter.reset();
		return lastSample;
	}
}
