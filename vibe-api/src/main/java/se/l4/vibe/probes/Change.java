package se.l4.vibe.probes;

import se.l4.vibe.probes.TimeSeries.Entry;

/**
 * Probes for detecting changes to the sampled values of a 
 * {@link TimeSeries time series}.
 * 
 * @author Andreas Holstenson
 *
 */
public class Change
{
	private Change()
	{
	}
	
	/**
	 * Create a probe that will return the numeric change for the given series.
	 * 
	 * @param series
	 * @return
	 */
	public static <T extends Number> Probe<Number> forSeries(TimeSeries<T> series)
	{
		return new ChangeProbe<T>(series);
	}
	
	/**
	 * Create a probe that will return the change as a fraction for the given 
	 * series.
	 * 
	 * @param series
	 * @return
	 */
	public static <T extends Number> Probe<Double> asFraction(TimeSeries<T> series)
	{
		return new ChangeAsFractionProbe<T>(series);
	}
	
	private static class ChangeProbe<T extends Number>
		implements Probe<Number>
	{
		private double lastValue;
		private double value;
		
		public ChangeProbe(TimeSeries<T> series)
		{
			lastValue = Double.NaN;
			
			series.addListener(new SampleListener<T>()
			{
				@Override
				public void sampleAcquired(SampledProbe<T> probe, Entry<T> entry)
				{
					double current = entry.getValue().doubleValue();
					value = current - lastValue;
					lastValue = current;
				}
			});
		}

		@Override
		public Number read()
		{
			return value;
		}
	}
	
	private static class ChangeAsFractionProbe<T extends Number>
		implements Probe<Double>
	{
		private double lastValue;
		private double value;
		
		public ChangeAsFractionProbe(TimeSeries<T> series)
		{
			lastValue = Double.NaN;
			
			series.addListener(new SampleListener<T>()
			{
				@Override
				public void sampleAcquired(SampledProbe<T> probe, Entry<T> entry)
				{
					double current = entry.getValue().doubleValue();
					value = current == lastValue 
						? 0 
						: (current - lastValue) / lastValue;
					
					lastValue = current;
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
