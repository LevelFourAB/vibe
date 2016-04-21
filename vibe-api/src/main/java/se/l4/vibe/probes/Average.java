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
	public static <T extends Number> Probe<Double> forSampler(Sampler<T> series)
	{
		return SamplerProbes.forSampler(series, Average.<T>newOperation());
	}
	
	/**
	 * Create an average for the entire time series.
	 * 
	 * @param series
	 * @param reader
	 * @return
	 */
	public static <I, T extends Number> Probe<Double> forSampler(Sampler<I> series, ValueReader<I, T> reader)
	{
		return SamplerProbes.forSampler(series, Average.<I, T>newOperation(reader));
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
	public static <T extends Number> Probe<Double> forSampler(
			Sampler<T> series,
			long duration,
			TimeUnit unit)
	{
		return SamplerProbes.forSampler(series, duration, unit, Average.<T>newOperation());
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
	public static <I, T extends Number> Probe<Double> forSampler(
			Sampler<I> series,
			ValueReader<I, T> reader,
			long duration,
			TimeUnit unit)
	{
		return SamplerProbes.forSampler(series, duration, unit, Average.<I, T>newOperation(reader));
	}
	
	/**
	 * Create a probe that will average another sampled probe. This type of
	 * probe should be used as part of a {@link Sampler time series}.
	 * 
	 * @param probe
	 * @return
	 */
	public static <T extends Number> SampledProbe<Double> forProbe(
			SampledProbe<T> probe)
	{
		return new AveragingProbeProbe<T>(probe);
	}
	
	/**
	 * Create a probe that averages numeric values.
	 * 
	 * @return
	 */
	public static AveragingProbe create()
	{
		return new AveragingProbe();
	}
	
	/**
	 * Create an operation that will calculate an average.
	 * 
	 * @return
	 */
	public static <T extends Number> SampleOperation<T, Double> newOperation()
	{
		return new AverageOperation<T, T>(ValueReaders.<T>same());
	}
	
	/**
	 * Create an operation that will calculate an average.
	 * 
	 * @return
	 */
	public static <I, T extends Number> SampleOperation<I, Double> newOperation(ValueReader<I, T> reader)
	{
		return new AverageOperation<I, T>(reader);
	}
	
	/**
	 * Operation that will calculate the average.
	 * 
	 * @author Andreas Holstenson
	 *
	 * @param <T>
	 */
	private static class AverageOperation<I, O extends Number>
		implements SampleOperation<I, Double>
	{
		private final ValueReader<I, O> reader;
		
		private double totalSum;
		private double totalEntries;
		
		public AverageOperation(ValueReader<I, O> reader)
		{
			this.reader = reader;
		}
		
		@Override
		public void add(I value, Collection<Sampler.Entry<I>> entries)
		{
			totalSum += reader.read(value).doubleValue();
			totalEntries += 1;
		}

		@Override
		public void remove(I value, Collection<Sampler.Entry<I>> entries)
		{
			totalSum -= reader.read(value).doubleValue();
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
	private static class AveragingProbeProbe<T extends Number>
		extends AbstractSampledProbe<Double>
	{
		private long accumulated;
		private long samples;
		private final SampledProbe<T> probe;

		public AveragingProbeProbe(SampledProbe<T> probe)
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
