package se.l4.vibe.operations;

import java.time.Duration;
import java.util.Collection;

import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.SampleListOperation;
import se.l4.vibe.sampling.SampleOperation;
import se.l4.vibe.sampling.Sampler;

/**
 * Operations for calculating averages of samples from a {@link Sampler}.
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
	public static <T extends Number> SampleOperation<T, Double> average()
	{
		return new AveragingSampleOperation<>();
	}

	/**
	 * Create an operation that will keep a rolling average for the specified
	 * duration.
	 *
	 * @param duration
	 * @return
	 */
	public static <T extends Number> SampleOperation<T, Double> averageOver(
		Duration duration
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

	/**
	 * Operation that averages all of the values ever sampled.
	 *
	 * @param <I>
	 */
	private static class AveragingSampleOperation<I extends Number>
		implements SampleOperation<I, Double>
	{
		private double accumulated;
		private long samples;

		@Override
		public Sample<Double> handleSample(Sample<I> sample)
		{
			accumulated += sample.getValue().doubleValue();
			samples++;
			return Sample.create(sample.getTime(), accumulated / samples);
		}
	}
}
