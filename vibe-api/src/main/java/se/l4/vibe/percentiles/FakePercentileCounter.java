package se.l4.vibe.percentiles;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link PercentileCounter} that only counts samples and the total value.
 */
public class FakePercentileCounter
	implements PercentileCounter
{
	private final AtomicLong samples;
	private final AtomicLong total;

	public FakePercentileCounter()
	{
		samples = new AtomicLong();
		total = new AtomicLong();
	}

	@Override
	public void add(long value)
	{
		total.addAndGet(value);
		samples.incrementAndGet();
	}

	@Override
	public PercentileSnapshot get()
	{
		long total = this.total.get();
		long samples = this.samples.get();

		return new FakeSnapshot(samples, total);
	}

	@Override
	public void reset()
	{
		total.set(0);
		samples.set(0);
	}

	private static class FakeSnapshot
		implements PercentileSnapshot
	{
		private final long samples;
		private final long total;

		public FakeSnapshot(long samples, long total)
		{
			this.samples = samples;
			this.total = total;
		}

		@Override
		public long getSamples()
		{
			return samples;
		}

		@Override
		public long getTotal()
		{
			return total;
		}

		@Override
		public long estimatePercentile(int percentile)
		{
			return -1;
		}

		@Override
		public PercentileSnapshot add(PercentileSnapshot other)
		{
			return new FakeSnapshot(samples + other.getSamples(), total + other.getTotal());
		}

		@Override
		public PercentileSnapshot remove(PercentileSnapshot other)
		{
			return new FakeSnapshot(samples - other.getSamples(), total - other.getTotal());
		}
	}
}
