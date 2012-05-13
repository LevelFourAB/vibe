package se.l4.vibe.probes;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Sampler that will sample certain {@link SampledProbe}s at a given time
 * interval.
 * 
 * @author Andreas Holstenson
 *
 */
public class TimeSeriesSampler
{
	private static final Logger logger = LoggerFactory.getLogger(TimeSeriesSampler.class);
	
	private static final TimeSeriesImpl<?>[] EMPTY = new TimeSeriesImpl[0];
	
	private final long sampleInterval;
	private final int maxSamples;
	
	private final LinkedBlockingDeque<SampleInfo> samples;
	
	private final Lock seriesLock;
	protected volatile TimeSeriesImpl[] series;
	
	private final ScheduledExecutorService executor;

	public TimeSeriesSampler(ScheduledExecutorService executor, long sampleInterval, long retention, TimeUnit unit)
	{
		this.executor = executor;
		
		this.sampleInterval = unit.toMillis(sampleInterval);
		maxSamples = (int) (unit.toMillis(retention) / this.sampleInterval);
		
		samples = new LinkedBlockingDeque<SampleInfo>(maxSamples);
		seriesLock = new ReentrantLock();
		series = EMPTY;
	}
	
	/**
	 * Add a statistic that should be sampled.
	 * 
	 * @param name
	 * @param stat
	 * @return
	 */
	public <T> TimeSeries<T> add(SampledProbe<T> stat)
	{
		TimeSeriesImpl<T> series = new TimeSeriesImpl<T>(stat, maxSamples, samples.size());
		
		// Store in series array
		seriesLock.lock();
		try
		{
			TimeSeriesImpl<?>[] current = this.series;
			TimeSeriesImpl<?>[] newSeries = new TimeSeriesImpl<?>[current.length + 1];
			System.arraycopy(current, 0, newSeries, 0, current.length);
			newSeries[current.length] = series;
			
			this.series = newSeries;
		}
		finally
		{
			seriesLock.unlock();
		}
		
		return series;
	}
	
	/**
	 * Start the sampling.
	 * 
	 * @return
	 */
	public void start()
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				sample();
			}
		};
		
		long now = System.currentTimeMillis();
		long rounded = (now / sampleInterval) * sampleInterval + sampleInterval;
		executor.scheduleAtFixedRate(r, rounded - now, sampleInterval, TimeUnit.MILLISECONDS);
	}
	
	protected void sample()
	{
		SampleInfo info = new SampleInfo(System.currentTimeMillis());
		add(samples, info);
		
		TimeSeriesImpl[] series = this.series;
		for(TimeSeriesImpl s : series)
		{
			s.sample();
		}
	}
	
	/**
	 * Perform an add on a bounded {@link LinkedBlockingDeque} by removing the
	 * first value if the queue is full.
	 * 
	 * @param <T>
	 * @param queue
	 * @param value
	 */
	private static <T> void add(LinkedBlockingDeque<T> queue, T value)
	{
		synchronized(queue)
		{
			if(queue.remainingCapacity() == 0) queue.removeFirst();
			queue.addLast(value);
		}
	}
	
	private static class SampleInfo
	{
		private final long time;

		public SampleInfo(long time)
		{
			this.time = time;
		}
	}
	
	private class TimeSeriesImpl<T>
		extends AbstractTimeSeries<T>
	{
		private final SampledProbe<T> probe;
		private final LinkedBlockingDeque<T> data;
		
		public TimeSeriesImpl(SampledProbe<T> probe, int maxSize, int currentSize)
		{
			this.probe = probe;
			
			data = new LinkedBlockingDeque<T>(maxSize);
			
			// Fill the data with empty values
			for(int i=0, n=currentSize; i<n; i++) data.addLast(null);
		}

		public void sample()
		{
			T value = probe.sample();
			add(data, value);
			
			SampleListener<T>[] listeners = this.listeners;
			if(listeners.length > 0)
			{
				SampleInfo sample = samples.getLast();
				SampleEntry<T> entry = new SampleEntry<T>(sample.time, value);
				
				for(SampleListener<T> listener : listeners)
				{
					try
					{
						listener.sampleAcquired(probe, entry);
					}
					catch(Exception e)
					{
						logger.warn("Unable to execute listener; " + e.getMessage(), e);
					}
				}
			}
		}
		
		@Override
		public SampledProbe<T> getProbe()
		{
			return probe;
		}
		
		@Override
		public Iterator<Entry<T>> iterator()
		{
			final SampleInfo[] info = samples.toArray(new SampleInfo[0]);
			final Object[] entries = data.toArray();
			
			return new Iterator<TimeSeries.Entry<T>>()
			{
				private int pointer = 0;
				
				@Override
				public boolean hasNext()
				{
					return pointer < info.length;
				}
				
				@Override
				public Entry<T> next()
				{
					SampleInfo s = info[pointer];
					Object d = entries[pointer];
					pointer++;
					
					return new SampleEntry(s.time, d);
				}
				
				@Override
				public void remove()
				{
					throw new UnsupportedOperationException();
				}
			};
		}
	}
	
	private static class SampleEntry<T>
		implements TimeSeries.Entry<T>
	{
		private final long time;
		private final T value;

		public SampleEntry(long time, T value)
		{
			this.time = time;
			this.value = value;
		}

		@Override
		public long getTime()
		{
			return time;
		}

		@Override
		public T getValue()
		{
			return value;
		}
		
		@Override
		public String toString()
		{
			return "Entry{time=" + time + ", value=" + value + "}";
		}
	}
}
