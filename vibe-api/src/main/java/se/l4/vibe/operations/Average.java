package se.l4.vibe.operations;

import java.time.Duration;
import java.util.Collection;

import edu.umd.cs.findbugs.annotations.NonNull;
import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.SampleListOperation;
import se.l4.vibe.sampling.TimeSampler;

/**
 * Operations for calculating averages of samples from a {@link TimeSampler}.
 */
public class Average
{
	private Average()
	{
	}

	/**
	 * Operation that will average all values ever sampled.
	 *
	 * @return
	 */
	@NonNull
	public static <T extends Number> Operation<T, Double> average()
	{
		return () -> new OperationExecutor<T, Double>()
		{
			private double accumulated;
			private long samples;

			@Override
			public Double apply(T sample)
			{
				accumulated += sample.doubleValue();
				samples++;
				return accumulated / samples;
			}
		};
	}

	/**
	 * Create an operation that will keep a rolling average for the specified
	 * duration.
	 *
	 * @param duration
	 * @return
	 */
	@NonNull
	public static <T extends Number> TimeSampleOperation<T, Double> averageOver(
		@NonNull Duration duration
	)
	{
		return TimeLimited.rollingOver(duration, new AverageSampleListOperation<>());
	}

	/**
	 * Operation that will calculate the average.
	 *
	 * @param <I>
	 */
	private static class AverageSampleListOperation<I extends Number>
		implements SampleListOperation<I, Double>
	{
		private double totalSum;
		private double totalEntries;

		@Override
		public void add(Sample<I> sample, Collection<Sample<I>> samples)
		{
			totalSum += sample.getValue().doubleValue();
			totalEntries += 1;
		}

		@Override
		public void remove(Sample<I> sample, Collection<Sample<I>> samples)
		{
			totalSum -= sample.getValue().doubleValue();
			totalEntries -= 1;
		}

		@Override
		public Double get()
		{
			return totalSum / totalEntries;
		}
	}
}
