package se.l4.vibe.operations;

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
	public static <T extends Number> Operation<T, Double> changeAsDouble()
	{
		return () -> new OperationExecutor<T, Double>()
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
	public static <T extends Number> Operation<T, Long> changeAsLong()
	{
		return () -> new OperationExecutor<T, Long>()
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
	public static <T extends Number> Operation<T, Double> changeAsFraction()
	{
		return () -> new OperationExecutor<T, Double>()
		{
			private double lastValue = Double.NaN;

			@Override
			public Double apply(T input)
			{
				double current = input.doubleValue();
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
				return change;
			}
		};
	}
}
