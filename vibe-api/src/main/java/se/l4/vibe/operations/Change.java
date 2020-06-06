package se.l4.vibe.operations;

import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.SampleOperation;

/**
 * Operations for detecting changes to samples values.
 */
public class Change
{
	private Change()
	{
	}

	/**
	 * Create an operation that will report the absolute change as a double.
	 *
	 * @return
	 */
	public static <T extends Number> SampleOperation<T, Double> changeAsDouble()
	{
		return new SampleOperation<T, Double>()
		{
			private double lastValue = 0;

			@Override
			public Sample<Double> handleSample(Sample<T> sample)
			{
				double current = sample.getValue().doubleValue();
				double change = current - lastValue;
				lastValue = current;
				return Sample.create(sample.getTime(), change);
			}
		};
	}

	/**
	 * Create an operation that will report the absolute change as a long.
	 *
	 * @return
	 */
	public static <T extends Number> SampleOperation<T, Long> changeAsLong()
	{
		return new SampleOperation<T, Long>()
		{
			private long lastValue = 0;

			@Override
			public Sample<Long> handleSample(Sample<T> sample)
			{
				long current = sample.getValue().longValue();
				long change = current - lastValue;
				lastValue = current;
				return Sample.create(sample.getTime(), change);
			}
		};
	}

	/**
	 * Create an operation that will return the change as fraction.
	 *
	 * @param series
	 * @return
	 */
	public static <T extends Number> SampleOperation<T, Double> changeAsFraction()
	{
		return new SampleOperation<T, Double>()
		{
			private double lastValue = Double.NaN;

			@Override
			public Sample<Double> handleSample(Sample<T> sample)
			{
				double current = sample.getValue().doubleValue();
				double change;
				if(Double.isNaN(lastValue))
				{
					change = 1;
				}
				else if(current == lastValue)
				{
					change = 0;
				}
				else
				{
					change = (current - lastValue) / lastValue;
				}
				lastValue = current;
				return Sample.create(sample.getTime(), change);
			}
		};
	}
}
