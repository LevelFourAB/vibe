package se.l4.vibe.operations;

import java.time.Duration;
import java.util.Collection;

import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.SampleListOperation;

/**
 * Operations that can calculate range values such as maximum and minimum
 * values.
 *
 */
public class Range
{
	private Range()
	{
	}

	/**
	 * Create an operation that calculates the all time minimum sampled value
	 * and returns it as a double.
	 *
	 * @return
	 */
	public static <T extends Number> Operation<T, Double> minAsDouble()
	{
		return () -> new OperationExecutor<T, Double>()
		{
			private double min = Double.MAX_VALUE;

			@Override
			public Double apply(T input)
			{
				double value = input.doubleValue();
				if(value < min) min = value;
				return min;
			}
		};
	}

	/**
	 * Create an operation that calculates the all time minimum sampled value
	 * and returns it as a long.
	 *
	 * @return
	 */
	public static <T extends Number> Operation<T, Long> minAsLong()
	{
		return () -> new OperationExecutor<T, Long>()
		{
			private long min = Long.MAX_VALUE;

			@Override
			public Long apply(T input)
			{
				long value = input.longValue();
				if(value < min) min = value;
				return min;
			}
		};
	}

	/**
	 * Create an operation that keeps track of the rolling minimum over the
	 * given duration and returns it as a double.
	 *
	 * @param <T>
	 * @param duration
	 * @return
	 */
	public static <T extends Number> TimeSampleOperation<T, Double> minAsDoubleOver(
		Duration duration
	)
	{
		return TimeLimited.rollingOver(duration, new MinDoubleOperation<>());
	}

	/**
	 * Create an operation that keeps track of the rolling minimum over the
	 * given duration and returns it as a long.
	 *
	 * @param <T>
	 * @param duration
	 * @return
	 */
	public static <T extends Number> TimeSampleOperation<T, Long> minAsLongOver(
		Duration duration
	)
	{
		return TimeLimited.rollingOver(duration, new MinLongOperation<>());
	}

	/**
	 * Create an operation that calculates the all time maximum sampled value
	 * and returns it as a double.
	 *
	 * @return
	 */
	public static <T extends Number> Operation<T, Double> maxAsDouble()
	{
		return () -> new OperationExecutor<T, Double>()
		{
			private double max = Double.MIN_VALUE;

			@Override
			public Double apply(T input)
			{
				double value = input.doubleValue();
				if(value > max) max = value;
				return max;
			}
		};
	}

	/**
	 * Create an operation that calculates the all time maximum sampled value
	 * and returns it as a long.
	 *
	 * @return
	 */
	public static <T extends Number> Operation<T, Long> maxAsLong()
	{
		return () -> new OperationExecutor<T, Long>()
		{
			private long max = Long.MIN_VALUE;

			@Override
			public Long apply(T input)
			{
				long value = input.longValue();
				if(value > max) max = value;
				return max;
			}
		};
	}

	/**
	 * Create an operation that keeps track of the rolling maximum over the
	 * given duration and returns it as a double.
	 *
	 * @param <T>
	 * @param duration
	 * @return
	 */
	public static <T extends Number> TimeSampleOperation<T, Double> maxAsDoubleOver(
		Duration duration
	)
	{
		return TimeLimited.rollingOver(duration, new MaxDoubleOperation<>());
	}

	/**
	 * Create an operation that keeps track of the rolling maximum over the
	 * given duration and returns it as a long.
	 *
	 * @param <T>
	 * @param duration
	 * @return
	 */
	public static <T extends Number> TimeSampleOperation<T, Long> maxAsLongOver(
		Duration duration
	)
	{
		return TimeLimited.rollingOver(duration, new MaxLongOperation<>());
	}

	private static class MinDoubleOperation<I extends Number>
		implements SampleListOperation<I, Double>
	{
		private double value;

		@Override
		public void add(Sample<I> sample, Collection<Sample<I>> samples)
		{
			double min = Double.MAX_VALUE;
			for(Sample<I> s : samples)
			{
				min = Math.min(min, s.getValue().doubleValue());
			}

			this.value = min;
		}

		@Override
		public void remove(Sample<I> sample, Collection<Sample<I>> samples)
		{
		}

		@Override
		public Double get()
		{
			return value;
		}
	}

	private static class MinLongOperation<I extends Number>
		implements SampleListOperation<I, Long>
	{
		private long value;

		@Override
		public void add(Sample<I> sample, Collection<Sample<I>> samples)
		{
			long min = Long.MAX_VALUE;
			for(Sample<I> s : samples)
			{
				min = Math.min(min, s.getValue().longValue());
			}

			this.value = min;
		}

		@Override
		public void remove(Sample<I> sample, Collection<Sample<I>> samples)
		{
		}

		@Override
		public Long get()
		{
			return value;
		}
	}

	private static class MaxDoubleOperation<I extends Number>
		implements SampleListOperation<I, Double>
	{
		private double value;

		@Override
		public void add(Sample<I> sample, Collection<Sample<I>> samples)
		{
			double max = Double.MIN_VALUE;
			for(Sample<I> s : samples)
			{
				max = Math.max(max, s.getValue().doubleValue());
			}

			this.value = max;
		}

		@Override
		public void remove(Sample<I> sample, Collection<Sample<I>> samples)
		{
		}

		@Override
		public Double get()
		{
			return value;
		}
	}

	private static class MaxLongOperation<I extends Number>
		implements SampleListOperation<I, Long>
	{
		private long value;

		@Override
		public void add(Sample<I> sample, Collection<Sample<I>> samples)
		{
			long max = Long.MIN_VALUE;
			for(Sample<I> s : samples)
			{
				max = Math.max(max, s.getValue().longValue());
			}

			this.value = max;
		}

		@Override
		public void remove(Sample<I> sample, Collection<Sample<I>> samples)
		{
		}

		@Override
		public Long get()
		{
			return value;
		}
	}
}
