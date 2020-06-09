package se.l4.vibe.percentiles;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

import se.l4.vibe.VibeException;

/**
 * A {@link PercentileCounter} that uses a fixed set of buckets that values are
 * sorted into.
 *
 * <p>
 * Each bucket represent a range, the first value given in this array is the start
 * of the first bucket and the last one is the upper bound for the <i>next to last</i>
 * bucket.
 *
 * <p>
 * Example:
 * <pre>
 * [0, 100, 400, 500]
 * </pre>
 *
 * Buckets created:
 * <ol>
 *   <li>0-100</li>
 *   <li>101-400</li>
 *   <li>401-500</li>
 *   <li>501-*</li>
 * </ol>
 */
public class BucketPercentileCounter
	implements PercentileCounter
{
	private final int[] limits;
	private volatile AtomicLongArray buckets;
	private final AtomicLong total;

	public BucketPercentileCounter(int... limits)
	{
		for(int i=1, n=limits.length; i<n; i++)
		{
			if(limits[i-1] >= limits[i])
			{
				throw new VibeException("Limits must be in ascending order");
			}
		}

		this.limits = limits;
		buckets = new AtomicLongArray(limits.length);
		total = new AtomicLong();
	}

	@Override
	public void add(long value)
	{
		int i = getBucket((int) value);
		if(i == -1) return;

		while(true)
		{
			long current = total.get();
			if(total.compareAndSet(current, current+value))
			{
				buckets.incrementAndGet(i);
				break;
			}
		}
	}

	@Override
	public void reset()
	{
		AtomicLongArray buckets = new AtomicLongArray(limits.length);

		while(true)
		{
			long current = total.get();
			if(total.compareAndSet(current, 0))
			{
				this.buckets = buckets;
				break;
			}
		}
	}

	@Override
	public PercentileSnapshot get()
	{
		long[] values = new long[limits.length];
		int samples = 0;
		for(int i=0, n=limits.length; i<n; i++)
		{
			values[i] = buckets.get(i);
			samples += values[i];
		}
		long total = this.total.get();

		return new BucketSnapshot(samples, total, values, limits);
	}

	int getBucket(int time)
	{
		int low = 0;
		int high = limits.length - 1;
		int i = 0;
		while(low <= high)
		{
			i = (low + high) / 2;
			int d = limits[i] - time;
			if(d < 0)
			{
				low = i + 1;
			}
			else if(d > 0)
			{
				high = i - 1;
			}
			else
			{
				return i;
			}
		}

		if(limits[i] >= time)
		{
			return i - 1;
		}
		else
		{
			return i;
		}
	}

	private static class BucketSnapshot
		implements PercentileSnapshot
	{
		private long samples;
		private long total;
		private long[] buckets;
		private int[] limits;

		public BucketSnapshot(long samples, long total, long[] buckets, int[] limits)
		{
			this.samples = samples;
			this.total = total;
			this.buckets = buckets;
			this.limits = limits;
		}

		@Override
		public long getTotal()
		{
			return total;
		}

		@Override
		public long getSamples()
		{
			return samples;
		}

		@Override
		public long estimatePercentile(int percentile)
		{
			long sum = 0;
			long cutoff = (long) Math.ceil((percentile / 100.0) * samples) - 1;
			for(int i=0, n=buckets.length-1; i<n; i++)
			{
				sum += buckets[i];
				if(sum >= cutoff)
				{
					return limits[i+1];
				}
			}

			return -1;
		}

		@Override
		public PercentileSnapshot add(PercentileSnapshot other)
		{
			BucketSnapshot s = (BucketSnapshot) other;
			long[] newBuckets = new long[buckets.length];
			for(int i=0, n=newBuckets.length; i<n; i++)
			{
				newBuckets[i] = buckets[i] + s.buckets[i];
			}
			return new BucketSnapshot(
				samples + s.samples,
				total + s.total,
				newBuckets,
				limits
			);
		}

		@Override
		public PercentileSnapshot remove(PercentileSnapshot other)
		{
			BucketSnapshot s = (BucketSnapshot) other;
			long[] newBuckets = new long[buckets.length];
			for(int i=0, n=newBuckets.length; i<n; i++)
			{
				newBuckets[i] = buckets[i] - s.buckets[i];
			}
			return new BucketSnapshot(
				samples - s.samples,
				total - s.total,
				newBuckets,
				limits
			);
		}
	}
}
