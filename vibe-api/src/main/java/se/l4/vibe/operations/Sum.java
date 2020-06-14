package se.l4.vibe.operations;

import java.time.Duration;
import java.util.Collection;

import edu.umd.cs.findbugs.annotations.NonNull;
import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.SampleListOperation;
import se.l4.vibe.sampling.TimeSampler;

/**
 * Operations for {@link TimeSampler} that calculate the sum of sampled values.
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
	@NonNull
	public static <T extends Number> Operation<T, Long> sumAsLong()
	{
		return () -> new OperationExecutor<T, Long>()
		{
			private long sum;

			@Override
			public Long apply(T value)
			{
				sum += value.longValue();
				return sum;
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
	@NonNull
	public static <T extends Number> Operation<T, Double> sumAsDouble()
	{
		return () -> new OperationExecutor<T, Double>()
		{
			private double sum;

			@Override
			public Double apply(T value)
			{
				sum += value.doubleValue();
				return sum;
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
	@NonNull
	public static <T extends Number> TimeSampleOperation<T, Long> sumAsLongOver(
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
	@NonNull
	public static <T extends Number> TimeSampleOperation<T, Double> sumAsDoubleOver(
		@NonNull Duration duration
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
