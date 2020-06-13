package se.l4.vibe.internal.timer;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

import se.l4.vibe.Handle;
import se.l4.vibe.internal.Listeners;
import se.l4.vibe.percentiles.BucketPercentileCounter;
import se.l4.vibe.percentiles.FakePercentileCounter;
import se.l4.vibe.percentiles.PercentileCounter;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.SampledProbe;
import se.l4.vibe.sampling.Sampler;
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
	private static final SnapshotSampler[] EMPTY = new SnapshotSampler[0];

	private final Listeners<TimerListener> listeners;

	private final Supplier<PercentileCounter> counterSupplier;
	private final SampledProbe<TimerSnapshot> snapshotProbe;

	private final AtomicLong min;
	private final AtomicLong max;
	private volatile SnapshotSampler[] samplers;

	private TimerImpl(Supplier<PercentileCounter> counter)
	{
		this.counterSupplier = counter;

		listeners = new Listeners<>();

		min = new AtomicLong();
		max = new AtomicLong();
		samplers = EMPTY;

		snapshotProbe = this::createSampler;
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

	@Override
	public Stopwatch start()
	{
		long time = System.nanoTime();
		return () -> {
			long now = System.nanoTime();
			long nowInMs = System.currentTimeMillis();

			long total = now - time;

			SnapshotSampler[] samplers = this.samplers;
			for(SnapshotSampler sampler : samplers)
			{
				sampler.add(total);
			}

			min.updateAndGet(c -> c > total ? total : c);
			max.updateAndGet(c -> c < total ? total : c);

			listeners.forEach(l -> l.timerEvent(nowInMs, total));
		};
	}

	@Override
	public Probe<Long> getMaximumInNSProbe()
	{
		return max::get;
	}

	@Override
	public Probe<Long> getMinimumInNSProbe()
	{
		return min::get;
	}

	@Override
	public SampledProbe<TimerSnapshot> getSnapshotProbe()
	{
		return snapshotProbe;
	}

	private Sampler<TimerSnapshot> createSampler()
	{
		SnapshotSampler sampler = new SnapshotSampler(
			counterSupplier.get(),
			this::releaseSampler
		);

		synchronized(this)
		{
			SnapshotSampler[] samplers = this.samplers;
			samplers = Arrays.copyOf(samplers, samplers.length + 1);
			samplers[samplers.length - 1] = sampler;
			this.samplers = samplers;
		}

		return sampler;
	}

	private void releaseSampler(SnapshotSampler sampler)
	{
		synchronized(this)
		{
			samplers = Arrays.stream(samplers)
				.filter(s -> s != sampler)
				.toArray(SnapshotSampler[]::new);
		}
	}

	private static class SnapshotSampler
		implements Sampler<TimerSnapshot>
	{
		private final Consumer<SnapshotSampler> remover;

		private final PercentileCounter counter;
		private final AtomicLong min;
		private final AtomicLong max;

		public SnapshotSampler(
			PercentileCounter counter,
			Consumer<SnapshotSampler> remover
		)
		{
			this.counter = counter;
			this.min = new AtomicLong();
			this.max = new AtomicLong();

			this.remover = remover;
		}

		@Override
		public TimerSnapshot sample()
		{
			TimerSnapshot snapshot = new TimerSnapshotImpl(counter.get(), min.get(), max.get());
			counter.reset();
			min.set(Long.MAX_VALUE);
			max.set(0);
			return snapshot;
		}

		public void add(long total)
		{
			counter.add(total);
			min.updateAndGet(c -> c > total ? total : c);
			max.updateAndGet(c -> c < total ? total : c);
		}

		@Override
		public void release()
		{
			remover.accept(this);
		}
	}

	public static class BuilderImpl
		implements Builder
	{
		private Supplier<PercentileCounter> percentileCounter;

		public BuilderImpl()
		{
			percentileCounter = FakePercentileCounter::new;
		}

		@Override
		public Builder withBuckets(int... limits)
		{
			int[] msLimits = new int[limits.length];
			for(int i=0, n=limits.length; i<n; i++)
			{
				msLimits[i] = limits[i] * 1000000;
			}
			return withPercentiles(() -> new BucketPercentileCounter(msLimits));
		}

		@Override
		public Builder withPercentiles(Supplier<PercentileCounter> counter)
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
