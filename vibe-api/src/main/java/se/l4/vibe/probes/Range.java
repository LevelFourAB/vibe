package se.l4.vibe.probes;

import java.util.Collection;
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
	public static <T extends Number> Probe<Double> minimum(
			TimeSeries<T> series,
			long duration,
			TimeUnit unit)
	{
		return TimeSeriesProbes.forSeries(series, duration, unit, new MinOperation<T>());
	}
	
	/**
	 * Return a probe that will return the maximum value measured over a
	 * certain period.
	 * 
	 * @param series
	 * @return
	 */
	public static <T extends Number> Probe<Double> maximum(
			TimeSeries<T> series,
			long duration,
			TimeUnit unit)
	{
		return TimeSeriesProbes.forSeries(series, duration, unit, new MaxOperation<T>());
	}
	
	/**
	 * Create a new operation that will calculate the minimum of any time
	 * series.
	 * 
	 * @return
	 */
	public static <T extends Number> TimeSeriesOperation<T, Double> newMinimumOperation()
	{
		return new MinOperation<T>();
	}
	
	private static class MinOperation<T extends Number>
		implements TimeSeriesOperation<T, Double>
	{
		private double value;

		@Override
		public void add(T value, Collection<TimeSeries.Entry<T>> entries)
		{
			double min = Double.MAX_VALUE;
			for(TimeSeries.Entry<T> entry : entries)
			{
				min = Math.min(min, entry.getValue().doubleValue());
			}
			
			this.value = min;
		}
		
		@Override
		public void remove(T value, Collection<TimeSeries.Entry<T>> entries)
		{
			// Do nothing
		}
		
		@Override
		public Double get()
		{
			return value;
		}
	}
	
	private static class MaxOperation<T extends Number>
		implements TimeSeriesOperation<T, Double>
	{
		private double value;
	
		@Override
		public void add(T value, Collection<TimeSeries.Entry<T>> entries)
		{
			double max = Double.MIN_VALUE;
			for(TimeSeries.Entry<T> entry : entries)
			{
				max = Math.min(max, entry.getValue().doubleValue());
			}
			
			this.value = max;
		}
		
		@Override
		public void remove(T value, Collection<TimeSeries.Entry<T>> entries)
		{
			// Do nothing
		}
		
		@Override
		public Double get()
		{
			return value;
		}
	}
	
	private static class SeriesMinMax<T extends Number>
		implements Probe<Double>
	{
		private double value;
		
		public SeriesMinMax(TimeSeries<T> series, final boolean min)
		{
			value = min ? Double.MAX_VALUE : Double.MIN_NORMAL;
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
}
