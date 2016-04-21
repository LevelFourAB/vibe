package se.l4.vibe.internal;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.vibe.probes.AbstractSampler;
import se.l4.vibe.probes.SampleListener;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.probes.Sampler;
import se.l4.vibe.probes.Sampler.Entry;


/**
 * Sampler that will sample certain {@link SampledProbe}s at a given time
 * interval.
 * 
 * @author Andreas Holstenson
 *
 */
public class SampleCollector
{
	private static final Logger logger = LoggerFactory.getLogger(SampleCollector.class);
	
	private static final TimeSeriesImpl<?>[] EMPTY = new TimeSeriesImpl[0];
	
	private final long sampleInterval;
	
	private final Lock seriesLock;
	protected volatile TimeSeriesImpl[] series;
	
	private final ScheduledExecutorService executor;

	public SampleCollector(ScheduledExecutorService executor, long sampleInterval, TimeUnit unit)
	{
		this.executor = executor;
		
		this.sampleInterval = unit.toMillis(sampleInterval);
		
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
	public <T> Sampler<T> add(SampledProbe<T> stat)
	{
		TimeSeriesImpl<T> series = new TimeSeriesImpl<T>(stat);
		
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
		long time = System.currentTimeMillis();
		
		TimeSeriesImpl[] series = this.series;
		for(TimeSeriesImpl s : series)
		{
			s.sample(time);
		}
	}
	
	private class TimeSeriesImpl<T>
		extends AbstractSampler<T>
	{
		private final SampledProbe<T> probe;
		
		public TimeSeriesImpl(SampledProbe<T> probe)
		{
			this.probe = probe;
		}

		public void sample(long time)
		{
			T value = probe.sample();
			
			SampleListener<T>[] listeners = this.listeners;
			if(listeners.length > 0)
			{
				SampleEntry<T> entry = new SampleEntry<T>(time, value);
				
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
	}
	
	private static class SampleEntry<T>
		implements Sampler.Entry<T>
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
