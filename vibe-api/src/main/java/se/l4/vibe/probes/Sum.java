package se.l4.vibe.probes;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.probes.TimeSeries.Entry;

/**
 * Probes that will calculate the sum of other {@link Probe probes} and
 * {@link TimeSeries time series}.
 * 
 * @author Andreas Holstenson
 *
 */
public class Sum
{
	private Sum()
	{
	}
	
	/**
	 * Create a probe that will calculate the sum of the entire series as a
	 * {@link Long}.
	 * 
	 * @param series
	 * @return
	 */
	public static <T extends Number> Probe<Long> forSeriesAsLong(TimeSeries<T> series)
	{
		return TimeSeriesProbes.forSeries(series, new SumLongOperation<T>());
	}
	
	/**
	 * Create a probe that will calculate the sum of the entire series as a
	 * {@link Double}.
	 * 
	 * @param series
	 * @return
	 */
	public static <T extends Number> Probe<Double> forSeriesAsDouble(TimeSeries<T> series)
	{
		return TimeSeriesProbes.forSeries(series, new SumDoubleOperation<T>());
	}
	
	/**
	 * Create a probe that will calculate the sum as a sliding window with
	 * the specified duration.
	 * 
	 * @param series
	 * @param duration
	 * @param unit
	 * @return
	 */
	public static <T extends Number> Probe<Long> forSeriesAsLong(
		TimeSeries<T> series,
		long duration, 
		TimeUnit unit
	)
	{
		return TimeSeriesProbes.forSeries(series, duration, unit, new SumLongOperation<T>());
	}
	
	/**
	 * Create a probe that will calculate the sum as a sliding window with
	 * the specified duration.
	 * 
	 * @param series
	 * @param duration
	 * @param unit
	 * @return
	 */
	public static <T extends Number> Probe<Double> forSeriesAsDouble(
		TimeSeries<T> series,
		long duration, 
		TimeUnit unit
	)
	{
		return TimeSeriesProbes.forSeries(series, duration, unit, new SumDoubleOperation<T>());
	}
	
	private static class SumDoubleOperation<T extends Number>
		implements TimeSeriesOperation<T, Double>
	{
		private double sum;

		@Override
		public void remove(T value, Collection<Entry<T>> entries)
		{
			sum -= value.doubleValue();
		}

		@Override
		public void add(T value, Collection<Entry<T>> entries)
		{
			sum += value.doubleValue();
		}

		@Override
		public Double get()
		{
			return sum;
		}
	}
	
	private static class SumLongOperation<T extends Number>
		implements TimeSeriesOperation<T, Long>
	{
		private long sum;
	
		@Override
		public void remove(T value, Collection<Entry<T>> entries)
		{
			sum -= value.longValue();
		}
	
		@Override
		public void add(T value, Collection<Entry<T>> entries)
		{
			sum += value.longValue();
		}
	
		@Override
		public Long get()
		{
			return sum;
		}
	}
}
