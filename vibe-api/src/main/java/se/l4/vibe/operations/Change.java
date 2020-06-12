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
	public static <T extends Number> SampleAndProbeOperation<T, Double> changeAsDouble()
	{
		return new SampleAndProbeOperation<T, Double>()
		{
			private double lastValue = 0;

			@Override
			public Double apply(T input)
			{
				double current = input.doubleValue();
				double change = current - lastValue;
				lastValue = current;
				return change;
			}
		};
	}

	/**
	 * Create an operation that will report the absolute change as a long.
	 *
	 * @return
	 */
	public static <T extends Number> SampleAndProbeOperation<T, Long> changeAsLong()
	{
		return new SampleAndProbeOperation<T, Long>()
		{
			private long lastValue = 0;

			@Override
			public Long apply(T input)
			{
				long current = input.longValue();
				long change = current - lastValue;
				lastValue = current;
				return change;
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
