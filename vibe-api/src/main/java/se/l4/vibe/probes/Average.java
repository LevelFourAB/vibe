package se.l4.vibe.probes;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.probes.TimeSeries.Entry;

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
		return new SeriesAverage<T>(series);
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
		return new SeriesTimedAverage<T>(series, unit.toMillis(duration));
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

	private static class SeriesAverage<T extends Number>
		implements Probe<Double>
	{
		private double totalSum;
		private long totalEntries;
		
		public SeriesAverage(TimeSeries<T> series)
		{
			series.addListener(new SampleListener<T>()
			{
				@Override
				public void sampleAcquired(SampledProbe<T> probe, TimeSeries.Entry<T> entry)
				{
					totalSum += entry.getValue().doubleValue();
					totalEntries += 1;
				}
			});
		}
		
		@Override
		public Double read()
		{
			return totalSum / totalEntries;
		}
	}
	
	private static class SeriesTimedAverage<T extends Number>
		implements Probe<Double>
	{
		private final List<TimeSeries.Entry<T>> entries;
		private final long maxAge;
		
		private double totalSum;
		private long totalEntries;
		
		public SeriesTimedAverage(TimeSeries<T> series, long maxAge0)
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
						Entry<T> firstEntry = entries.get(0);
						if(firstEntry.getTime() < System.currentTimeMillis() - maxAge)
						{
							entries.remove(0);
							
							totalSum = firstEntry.getValue().doubleValue();
							totalEntries -= 1;
						}
					}
					
					entries.add(entry);
					
					totalSum += entry.getValue().doubleValue();
					totalEntries += 1;
				}
			});
		}
		
		@Override
		public Double read()
		{
			return totalSum / totalEntries;
		}
	}
}
