package se.l4.vibe.probes;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Range operations for {@link TimeSeries time series}.
 * 
 * @author Andreas Holstenson
 *
 */
public class Range
{
	private Range()
	{
	}
	
	/**
	 * Return a probe that will always return the minimum value ever measured
	 * in the given series.
	 * 
	 * @param series
	 * @return
	 */
	public static <T extends Number> Probe<Double> min(TimeSeries<T> series)
	{
		return new SeriesMinMax<T>(series, true);
	}
	
	/**
	 * Return a probe that will always return the maximum value ever measured
	 * in the given series.
	 * 
	 * @param series
	 * @return
	 */
	public static <T extends Number> Probe<Double> max(TimeSeries<T> series)
	{
		return new SeriesMinMax<T>(series, false);
	}
	
	/**
	 * Return a probe that will return the minimum value measured over a
	 * certain period.
	 * 
	 * @param series
	 * @return
	 */
	public static <T extends Number> Probe<Double> min(
			TimeSeries<T> series,
			long duration,
			TimeUnit unit)
	{
		return new SeriesTimedMinMax<T>(series, unit.toMillis(duration), true);
	}
	
	/**
	 * Return a probe that will return the maximum value measured over a
	 * certain period.
	 * 
	 * @param series
	 * @return
	 */
	public static <T extends Number> Probe<Double> max(
			TimeSeries<T> series,
			long duration,
			TimeUnit unit)
	{
		return new SeriesTimedMinMax<T>(series, unit.toMillis(duration), false);
	}
	
	private static class SeriesMinMax<T extends Number>
		implements Probe<Double>
	{
		private double value;
		
		public SeriesMinMax(TimeSeries<T> series, final boolean min)
		{
			value = min ? Double.MIN_NORMAL : Double.MAX_VALUE;
			series.addListener(new SampleListener<T>()
			{
				@Override
				public void sampleAcquired(SampledProbe<T> probe, TimeSeries.Entry<T> entry)
				{
					double newValue = entry.getValue().doubleValue();
					if(min)
					{
						value = Math.min(newValue, value);
					}
					else
					{
						value = Math.max(newValue, value);
					}
				}
			});
		}
		
		@Override
		public Double read()
		{
			return value;
		}
	}
	
	private static class SeriesTimedMinMax<T extends Number>
		implements Probe<Double>
	{
		private final List<TimeSeries.Entry<T>> entries;
		private final long maxAge;
		
		private double value;
		
		public SeriesTimedMinMax(TimeSeries<T> series, long maxAge0, final boolean min)
		{
			this.maxAge = maxAge0;
			entries = new LinkedList<TimeSeries.Entry<T>>();
			
			series.addListener(new SampleListener<T>()
			{
				@Override
				public void sampleAcquired(SampledProbe<T> probe, TimeSeries.Entry<T> entry)
				{
					if(! entries.isEmpty())
					{
						/*
						 * If we have entries check if the first one should be
						 * removed or kept.
						 */
						TimeSeries.Entry<T> firstEntry = entries.get(0);
						if(firstEntry.getTime() < System.currentTimeMillis() - maxAge)
						{
							entries.remove(0);
						}
					}
					
					entries.add(entry);
					
					double newValue = 0;
					for(TimeSeries.Entry<T> e : entries)
					{
						double v = e.getValue().doubleValue();
						if(min)
						{
							newValue = Math.min(newValue, v);
						}
						else
						{
							newValue = Math.max(newValue, v);
						}
					}
					
					value = newValue;
				}
			});
		}
		
		@Override
		public Double read()
		{
			return value;
		}
	}
}
