package se.l4.vibe.internal.timer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

import se.l4.vibe.Handle;
import se.l4.vibe.internal.Listeners;
import se.l4.vibe.percentiles.BucketPercentileCounter;
import se.l4.vibe.percentiles.FakePercentileCounter;
import se.l4.vibe.percentiles.PercentileCounter;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.sampling.Sampler;
import se.l4.vibe.timers.Stopwatch;
import se.l4.vibe.timers.Timer;
import se.l4.vibe.timers.TimerEvent;
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

	private final TimeUnit resolution;

	private final Supplier<PercentileCounter> counterSupplier;
	private final SampledProbe<TimerSnapshot> snapshotProbe;

	private final AtomicLong min;
	private final AtomicLong max;
	private volatile SnapshotSampler[] samplers;

	private TimerImpl(
		TimeUnit resolution,
		Supplier<PercentileCounter> counter
	)
	{
		this.resolution = resolution;
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

			long totalInNs = now - time;
			long total = resolution.convert(totalInNs, TimeUnit.NANOSECONDS);

			SnapshotSampler[] samplers = this.samplers;
			for(SnapshotSampler sampler : samplers)
			{
				sampler.add(total);
			}

			min.updateAndGet(c -> c > total ? total : c);
			max.updateAndGet(c -> c < total ? total : c);

			TimerEvent event = new TimerEvent(resolution, total);
			listeners.forEach(l -> l.timingComplete(event));
		};
	}

	@Override
	public TimeUnit getResolution()
	{
		return resolution;
	}

	@Override
	public Probe<Long> getMaximumProbe()
	{
		return max::get;
	}

	@Override
	public Probe<Long> getMinimumProbe()
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
			resolution,
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

		private final TimeUnit resolution;
		private final PercentileCounter counter;
		private final AtomicLong min;
		private final AtomicLong max;

		public SnapshotSampler(
			TimeUnit resolution,
			PercentileCounter counter,
			Consumer<SnapshotSampler> remover
		)
		{
			this.resolution = resolution;

			this.counter = counter;
			this.min = new AtomicLong();
			this.max = new AtomicLong();

			this.remover = remover;
		}

		@Override
		public TimerSnapshot sample()
		{
			TimerSnapshot snapshot = new TimerSnapshotImpl(
				resolution,
				counter.get(),
				min.get(),
				max.get()
			);
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
		private TimeUnit resolution;

		private Duration[] buckets;

		public BuilderImpl()
		{
			percentileCounter = FakePercentileCounter::new;
			resolution = TimeUnit.MILLISECONDS;
		}

		@Override
		public Builder withResolution(TimeUnit unit)
		{
			Objects.requireNonNull(unit, "unit must be specified");
			this.resolution = unit;
			return this;
		}

		@Override
		public Builder withBuckets(Duration... limits)
		{
			Objects.requireNonNull(limits, "limits must be specified");

			this.buckets = limits;
			return this;
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
			if(buckets != null)
			{
				int[] limits = Arrays.stream(buckets)
					.mapToInt(d -> (int) resolution.convert(d.toNanos(), TimeUnit.NANOSECONDS))
					.toArray();

				percentileCounter = () -> new BucketPercentileCounter(limits);
			}

			return new TimerImpl(resolution, percentileCounter);
		}
	}
}
