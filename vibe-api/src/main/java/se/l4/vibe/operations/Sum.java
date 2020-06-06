package se.l4.vibe.operations;

import java.time.Duration;
import java.util.Collection;

import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.SampleListOperation;
import se.l4.vibe.sampling.SampleOperation;
import se.l4.vibe.sampling.Sampler;

/**
 * Operations for {@link Sampler} that calculate the sum of sampled values.
 */
public class Sum
{
	private Sum()
	{
	}

	/**
	 * Create an operation that will keep a sum of all the values ever sampled
	 * and make it available as a {@link Long}.
	 *
	 * @param <T>
	 * @return
	 */
	public static <T extends Number> SampleOperation<T, Long> sumAsLong()
	{
		return new SampleOperation<T, Long>()
		{
			private long sum;

			@Override
			public Sample<Long> handleSample(Sample<T> sample)
			{
				sum += sample.getValue().longValue();
				return Sample.create(sample.getTime(), sum);
			}
		};
	}

	/**
	 * Create an operation that will keep a sum of all the values ever sampled
	 * and make it available as a {@link Double}.
	 *
	 * @param <T>
	 * @return
	 */
	public static <T extends Number> SampleOperation<T, Double> sumAsDouble()
	{
		return new SampleOperation<T, Double>()
		{
			private double sum;

			@Override
			public Sample<Double> handleSample(Sample<T> sample)
			{
				sum += sample.getValue().doubleValue();
				return Sample.create(sample.getTime(), sum);
			}
		};
	}

	/**
	 * Create an operation that will keep a rolling sum of all values over the
	 * given duration of time and make it available as a {@link Long}.
	 *
	 * @param <T>
	 * @param duration
	 * @return
	 */
	public static <T extends Number> SampleOperation<T, Long> sumAsLongOver(
		Duration duration
	)
	{
		return TimeLimited.rollingOver(duration, new SumLongOperation<>());
	}

	/**
	 * Create an operation that will keep a rolling sum of all values over the
	 * given duration of time and make it available as a {@link Double}.
	 *
	 * @param <T>
	 * @param duration
	 * @return
	 */
	public static <T extends Number> SampleOperation<T, Double> sumAsDoubleOver(
		Duration duration
	)
	{
		return TimeLimited.rollingOver(duration, new SumDoubleOperation<>());
	}

	private static class SumDoubleOperation<I extends Number>
		implements SampleListOperation<I, Double>
	{
		private double sum;

		@Override
		public void add(Sample<I> sample, Collection<Sample<I>> samples)
		{
			sum += sample.getValue().doubleValue();
		}

		@Override
		public void remove(Sample<I> sample, Collection<Sample<I>> samples)
		{
			sum -= sample.getValue().doubleValue();
		}

		@Override
		public Double get()
		{
			return sum;
		}
	}

	private static class SumLongOperation<I extends Number>
		implements SampleListOperation<I, Long>
	{
		private long sum;

		@Override
		public void add(Sample<I> sample, Collection<Sample<I>> samples)
		{
			sum += sample.getValue().longValue();
		}

		@Override
		public void remove(Sample<I> sample, Collection<Sample<I>> samples)
		{
			sum -= sample.getValue().longValue();
		}

		@Override
		public Long get()
		{
			return sum;
		}
	}
}
