package se.l4.vibe.internal.timer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import se.l4.vibe.Handle;
import se.l4.vibe.internal.Listeners;
import se.l4.vibe.percentiles.BucketPercentileCounter;
import se.l4.vibe.percentiles.FakePercentileCounter;
import se.l4.vibe.percentiles.PercentileCounter;
import se.l4.vibe.timers.Stopwatch;
import se.l4.vibe.timers.Timer;
import se.l4.vibe.timers.TimerListener;
import se.l4.vibe.timers.TimerSnapshot;

/**
 * Implementation of {@link Timer}.
 */
public class TimerImpl
	implements Timer
{
	private final Listeners<TimerListener> listeners;

	private final PercentileCounter counter;
	private volatile TimerSnapshot lastSample;

	private final AtomicLong min;
	private final AtomicLong max;

	public TimerImpl(PercentileCounter counter)
	{
		this.counter = counter;

		listeners = new Listeners<>();

		min = new AtomicLong();
		max = new AtomicLong();
	}

	@Override
	public Handle addListener(TimerListener listener)
	{
		return listeners.add(listener);
	}

	@Override
	public void removeListener(TimerListener listener)
	{
		listeners.remove(listener);
	}

	private long time()
	{
		return System.nanoTime();
	}

	@Override
	public Stopwatch start()
	{
		long time = time();
		return () -> {
			long now = time();
			long nowInMs = System.currentTimeMillis();

			long total = now - time;
			counter.add(total);
			min.updateAndGet(c -> c > total ? total : c);
			max.updateAndGet(c -> c < total ? total : c);

			listeners.forEach(l -> l.timerEvent(nowInMs, total));
		};
	}

	private TimerSnapshot createSample()
	{
		return new TimerSnapshotImpl(counter.get(), min.get(), max.get());
	}

	@Override
	public TimerSnapshot sample()
	{
		lastSample = createSample();
		counter.reset();
		min.set(Long.MAX_VALUE);
		max.set(0);
		return lastSample;
	}

	public static class BuilderImpl
		implements Builder
	{
		private PercentileCounter percentileCounter;

		public BuilderImpl()
		{
			percentileCounter = new FakePercentileCounter();
		}

		@Override
		public Builder withBuckets(int... limits)
		{
			int[] msLimits = new int[limits.length];
			for(int i=0, n=limits.length; i<n; i++)
			{
				msLimits[i] = limits[i] * 1000000;
			}
			return withPercentiles(new BucketPercentileCounter(msLimits));
		}

		@Override
		public Builder withPercentiles(PercentileCounter counter)
		{
			Objects.requireNonNull(counter, "counter must be specified");

			this.percentileCounter = counter;
			return this;
		}

		@Override
		public Timer build()
		{
			return new TimerImpl(percentileCounter);
		}
	}
}
