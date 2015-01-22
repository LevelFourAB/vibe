package se.l4.vibe.probes;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Range operations for {@link Sampler time series}.
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
	public static <T extends Number> Probe<Double> min(Sampler<T> series)
	{
		return new SeriesMinMax<T>(series, ValueReaders.<T>same(), true);
	}
	
	/**
	 * Return a probe that will always return the minimum value ever measured
	 * in the given series.
	 * 
	 * @param series
	 * @return
	 */
	public static <T, N extends Number> Probe<Double> min(Sampler<T> series, ValueReader<T, N> reader)
	{
		return new SeriesMinMax<T>(series, reader, true);
	}
	
	/**
	 * Return a probe that will always return the maximum value ever measured
	 * in the given series.
	 * 
	 * @param series
	 * @return
	 */
	public static <T, N extends Number> Probe<Double> max(Sampler<T> series, ValueReader<T, N> reader)
	{
		return new SeriesMinMax<T>(series, reader, false);
	}
	
	/**
	 * Return a probe that will return the minimum value measured over a
	 * certain period.
	 * 
	 * @param series
	 * @return
	 */
	public static <T extends Number> Probe<Double> minimum(
			Sampler<T> series,
			long duration,
			TimeUnit unit)
	{
		return SamplerProbes.forSampler(series, duration, unit, new MinOperation<T, T>(ValueReaders.<T>same()));
	}
	
	/**
	 * Return a probe that will return the minimum value measured over a
	 * certain period.
	 * 
	 * @param series
	 * @return
	 */
	public static <T, N extends Number> Probe<Double> minimum(
			Sampler<T> series,
			ValueReader<T, N> reader,
			long duration,
			TimeUnit unit)
	{
		return SamplerProbes.forSampler(series, duration, unit, new MinOperation<T, N>(reader));
	}
	
	/**
	 * Return a probe that will return the maximum value measured over a
	 * certain period.
	 * 
	 * @param series
	 * @return
	 */
	public static <T extends Number> Probe<Double> maximum(
			Sampler<T> series,
			long duration,
			TimeUnit unit)
	{
		return SamplerProbes.forSampler(series, duration, unit, new MaxOperation<T, T>(ValueReaders.<T>same()));
	}
	
	/**
	 * Return a probe that will return the maximum value measured over a
	 * certain period.
	 * 
	 * @param series
	 * @return
	 */
	public static <T, N extends Number> Probe<Double> maximum(
			Sampler<T> series,
			ValueReader<T, N> reader,
			long duration,
			TimeUnit unit)
	{
		return SamplerProbes.forSampler(series, duration, unit, new MaxOperation<T, N>(reader));
	}
	
	/**
	 * Create a new operation that will calculate the minimum of any time
	 * series.
	 * 
	 * @return
	 */
	public static <T extends Number> SampleOperation<T, Double> newMinimumOperation()
	{
		return new MinOperation<T, T>(ValueReaders.<T>same());
	}

	/**
	 * Create a new operation that will calculate the minimum of any time
	 * series.
	 * 
	 * @return
	 */
	public static <T, N extends Number> SampleOperation<T, Double> newMinimumOperation(ValueReader<T, N> reader)
	{
		return new MinOperation<T, N>(reader);
	}
	
	/**
	 * Create a new operation that will calculate the minimum of any time
	 * series.
	 * 
	 * @return
	 */
	public static <T extends Number> SampleOperation<T, Double> newMaximumOperation()
	{
		return new MaxOperation<T, T>(ValueReaders.<T>same());
	}
	
	/**
	 * Create a new operation that will calculate the minimum of any time
	 * series.
	 * 
	 * @return
	 */
	public static <T, N extends Number> SampleOperation<T, Double> newMaximumOperation(ValueReader<T, N> reader)
	{
		return new MaxOperation<T, N>(reader);
	}
	
	private static class MinOperation<I, O extends Number>
		implements SampleOperation<I, Double>
	{
		private final ValueReader<I, O> reader;
		private double value;
		
		public MinOperation(ValueReader<I, O> reader)
		{
			this.reader = reader;
		}

		@Override
		public void add(I value, Collection<Sampler.Entry<I>> entries)
		{
			double min = Double.MAX_VALUE;
			for(Sampler.Entry<I> entry : entries)
			{
				min = Math.min(min, reader.read(entry.getValue()).doubleValue());
			}
			
			this.value = min;
		}
		
		@Override
		public void remove(I value, Collection<Sampler.Entry<I>> entries)
		{
			// Do nothing
		}
		
		@Override
		public Double get()
		{
			return value;
		}
	}
	
	private static class MaxOperation<I, T extends Number>
		implements SampleOperation<I, Double>
	{
		private final ValueReader<I, T> reader;
		private double value;
	
		public MaxOperation(ValueReader<I, T> reader)
		{
			this.reader = reader;
		}
		
		@Override
		public void add(I value, Collection<Sampler.Entry<I>> entries)
		{
			double max = Double.MIN_VALUE;
			for(Sampler.Entry<I> entry : entries)
			{
				max = Math.max(max, reader.read(entry.getValue()).doubleValue());
			}
			
			this.value = max;
		}
		
		@Override
		public void remove(I value, Collection<Sampler.Entry<I>> entries)
		{
			// Do nothing
		}
		
		@Override
		public Double get()
		{
			return value;
		}
	}
	
	private static class SeriesMinMax<T>
		implements Probe<Double>
	{
		private double value;
		
		public SeriesMinMax(Sampler<T> series, final ValueReader<T, ? extends Number> reader, final boolean min)
		{
			value = min ? Double.MAX_VALUE : Double.MIN_NORMAL;
			series.addListener(new SampleListener<T>()
			{
				@Override
				public void sampleAcquired(SampledProbe<T> probe, Sampler.Entry<T> entry)
				{
					double newValue = reader.read(entry.getValue()).doubleValue();
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
