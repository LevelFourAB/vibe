package se.l4.vibe.probes;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.probes.Sampler.Entry;

/**
 * Probes that will calculate the sum of other {@link Probe probes} and
 * {@link Sampler time series}.
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
	public static <T extends Number> Probe<Long> forSamplerAsLong(Sampler<T> series)
	{
		return SamplerProbes.forSampler(series, new SumLongOperation<T, T>(ValueReaders.<T>same()));
	}

	/**
	 * Create a probe that will calculate the sum of the entire series as a
	 * {@link Long}.
	 *
	 * @param series
	 * @return
	 */
	public static <T, N extends Number> Probe<Long> forSamplerAsLong(Sampler<T> series, ValueReader<T, N> reader)
	{
		return SamplerProbes.forSampler(series, new SumLongOperation<T, N>(reader));
	}

	/**
	 * Create a probe that will calculate the sum of the entire series as a
	 * {@link Double}.
	 *
	 * @param series
	 * @return
	 */
	public static <T extends Number> Probe<Double> forSamplerAsDouble(Sampler<T> series)
	{
		return SamplerProbes.forSampler(series, new SumDoubleOperation<T, T>(ValueReaders.<T>same()));
	}

	/**
	 * Create a probe that will calculate the sum of the entire series as a
	 * {@link Double}.
	 *
	 * @param series
	 * @return
	 */
	public static <T, N extends Number> Probe<Double> forSamplerAsDouble(Sampler<T> series, ValueReader<T, N> reader)
	{
		return SamplerProbes.forSampler(series, new SumDoubleOperation<T, N>(reader));
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
	public static <T extends Number> Probe<Long> forSamplerAsLong(
		Sampler<T> series,
		long duration,
		TimeUnit unit
	)
	{
		return SamplerProbes.forSampler(series, duration, unit, new SumLongOperation<T, T>(ValueReaders.<T>same()));
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
	public static <T, N extends Number> Probe<Long> forSamplerAsLong(
		Sampler<T> series,
		ValueReader<T, N> reader,
		long duration,
		TimeUnit unit
	)
	{
		return SamplerProbes.forSampler(series, duration, unit, new SumLongOperation<T, N>(reader));
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
	public static <T extends Number> Probe<Double> forSamplerAsDouble(
		Sampler<T> series,
		long duration,
		TimeUnit unit
	)
	{
		return SamplerProbes.forSampler(series, duration, unit, new SumDoubleOperation<T, T>(ValueReaders.<T>same()));
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
	public static <T, N extends Number> Probe<Double> forSamplerAsDouble(
		Sampler<T> series,
		ValueReader<T, N> reader,
		long duration,
		TimeUnit unit
	)
	{
		return SamplerProbes.forSampler(series, duration, unit, new SumDoubleOperation<T, N>(reader));
	}

	private static class SumDoubleOperation<T, N extends Number>
		implements SampleOperation<T, Double>
	{
		private final ValueReader<T, N> reader;

		private double sum;

		public SumDoubleOperation(ValueReader<T, N> reader)
		{
			this.reader = reader;
		}

		@Override
		public void remove(T value, Collection<Entry<T>> entries)
		{
			sum -= reader.read(value).doubleValue();
		}

		@Override
		public void add(T value, Collection<Entry<T>> entries)
		{
			sum += reader.read(value).doubleValue();
		}

		@Override
		public Double get()
		{
			return sum;
		}
	}

	private static class SumLongOperation<T, N extends Number>
		implements SampleOperation<T, Long>
	{
		private final ValueReader<T, N> reader;

		private long sum;

		public SumLongOperation(ValueReader<T, N> reader)
		{
			this.reader = reader;
		}

		@Override
		public void remove(T value, Collection<Entry<T>> entries)
		{
			sum -= reader.read(value).longValue();
		}

		@Override
		public void add(T value, Collection<Entry<T>> entries)
		{
			sum += reader.read(value).longValue();
		}

		@Override
		public Long get()
		{
			return sum;
		}
	}
}
