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
		return new ChangeProbe<T, T>(series, ValueReaders.<T>same());
	}
	
	/**
	 * Create a probe that will return the numeric change for the given series.
	 * 
	 * @param series
	 * @return
	 */
	public static <I, T extends Number> Probe<Number> forSeries(TimeSeries<I> series, ValueReader<I, T> reader)
	{
		return new ChangeProbe<I, T>(series, reader);
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
		return new ChangeAsFractionProbe<T, T>(series, ValueReaders.<T>same());
	}
	
	/**
	 * Create a probe that will return the change as a fraction for the given 
	 * series.
	 * 
	 * @param series
	 * @return
	 */
	public static <I, T extends Number> Probe<Double> asFraction(TimeSeries<I> series, ValueReader<I, T> reader)
	{
		return new ChangeAsFractionProbe<I, T>(series, reader);
	}
	
	private static class ChangeProbe<I, O extends Number>
		implements Probe<Number>
	{
		private double lastValue;
		private double value;
		
		public ChangeProbe(TimeSeries<I> series, final ValueReader<I, O> reader)
		{
			lastValue = Double.NaN;
			
			series.addListener(new SampleListener<I>()
			{
				@Override
				public void sampleAcquired(SampledProbe<I> probe, Entry<I> entry)
				{
					double current = reader.read(entry.getValue()).doubleValue();
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
	
	private static class ChangeAsFractionProbe<I, O extends Number>
		implements Probe<Double>
	{
		private double lastValue;
		private double value;
		
		public ChangeAsFractionProbe(TimeSeries<I> series, final ValueReader<I, O> reader)
		{
			lastValue = Double.NaN;
			
			series.addListener(new SampleListener<I>()
			{
				@Override
				public void sampleAcquired(SampledProbe<I> probe, Entry<I> entry)
				{
					double current = reader.read(entry.getValue()).doubleValue();
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
