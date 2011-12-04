package se.l4.vibe.probes;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Probes for creating average values.
 * 
 * @author Andreas Holstenson
 *
 */
public class Average
{
	private Average()
	{
	}
	
	/**
	 * Create an average for the entire time series.
	 * 
	 * @param series
	 * @return
	 */
	public static <T extends Number> Probe<Double> forSeries(TimeSeries<T> series)
	{
		return TimeSeriesProbes.forSeries(series, new AverageOperation<T>());
	}
	
	/**
	 * Create an average for a time series that will keep an average for the
	 * specified duration.
	 * 
	 * @param series
	 * @param duration
	 * @param unit
	 * @return
	 */
	public static <T extends Number> Probe<Double> forSeries(
			TimeSeries<T> series,
			long duration,
			TimeUnit unit)
	{
		return TimeSeriesProbes.forSeries(series, duration, unit, new AverageOperation<T>());
	}
	
	/**
	 * Create a probe that will average another sampled probe. This type of
	 * probe should be used as part of a {@link TimeSeries time series}.
	 * 
	 * @param probe
	 * @return
	 */
	public static <T extends Number> SampledProbe<Double> forProbe(
			SampledProbe<T> probe)
	{
		return new AveragingProbe<T>(probe);
	}
	
	/**
	 * Create an operation that will calculate an average.
	 * 
	 * @return
	 */
	public static <T extends Number> TimeSeriesOperation<T, Double> newOperation()
	{
		return new AverageOperation<T>();
	}

	/**
	 * Operation that will calculate the average.
	 * 
	 * @author Andreas Holstenson
	 *
	 * @param <T>
	 */
	private static class AverageOperation<T extends Number>
		implements TimeSeriesOperation<T, Double>
	{
		private double totalSum;
		private double totalEntries;
		
		@Override
		public void add(T value, Collection<TimeSeries.Entry<T>> entries)
		{
			totalSum += value.doubleValue();
			totalEntries += 1;
		}

		@Override
		public void remove(T value, Collection<TimeSeries.Entry<T>> entries)
		{
			totalSum -= value.doubleValue();
			totalEntries -= 1;
		}
		
		@Override
		public Double get()
		{
			return totalSum / totalEntries;
		}
	}
	
	/**
	 * Probe that calculates the average of another probe.
	 * 
	 * @author Andreas Holstenson
	 *
	 * @param <T>
	 */
	private static class AveragingProbe<T extends Number>
		extends AbstractSampledProbe<Double>
	{
		private long accumulated;
		private long samples;
		private final SampledProbe<T> probe;

		public AveragingProbe(SampledProbe<T> probe)
		{
			this.probe = probe;
		}

		@Override
		public Double peek()
		{
			long diff = probe.peek().longValue();
			return (accumulated + diff) / (samples + 1.0);
		}

		@Override
		protected Double sample0()
		{
			accumulated += probe.read().longValue(); 
			samples++;
			return accumulated / (double) samples;
		}

	}
}
