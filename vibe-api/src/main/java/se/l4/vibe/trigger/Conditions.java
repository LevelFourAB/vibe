package se.l4.vibe.trigger;

/**
 * Conditions available for use with {@link Check checks}.
 */
public class Conditions
{
	private Conditions()
	{
	}

	/**
	 * Check that the value is equal to the given instance.
	 *
	 * @param instance
	 * @return
	 */
	public static <T> Condition<T> is(final T instance)
	{
		return new Condition<T>()
		{
			public boolean matches(T value)
			{
				if(value == null && instance != null)
				{
					return false;
				}
				else if(value != null && instance == null)
				{
					return false;
				}
				else
				{
					return instance.equals(value);
				}
			}
		};
	}

	/**
	 * Check that a value is equal to the specified number.
	 *
	 * @param number
	 * @return
	 */
	public static Condition<Number> is(final double number)
	{
		return new Condition<Number>()
		{
			public boolean matches(Number value)
			{
				if(value == null)
				{
					return false;
				}

				return value.doubleValue() == number;
			}
		};
	}

	/**
	 * Check that a value is equal to the specified number.
	 *
	 * @param number
	 * @return
	 */
	public static Condition<Number> is(final int number)
	{
		return new Condition<Number>()
		{
			public boolean matches(Number value)
			{
				if(value == null)
				{
					return false;
				}

				return value.intValue() == number;
			}
		};
	}

	/**
	 * Check that a value is equal to the specified number.
	 *
	 * @param number
	 * @return
	 */
	public static Condition<Number> is(final long number)
	{
		return new Condition<Number>()
		{
			public boolean matches(Number value)
			{
				if(value == null)
				{
					return false;
				}

				return value.longValue() == number;
			}
		};
	}

	/**
	 * Check that a value is within the given range. Lower is inclusive,
	 * while upper is exclusive.
	 *
	 * @param number
	 * @return
	 */
	public static Condition<Number> inRange(final double lower, final double upper)
	{
		return new Condition<Number>()
		{
			public boolean matches(Number value)
			{
				if(value == null)
				{
					return false;
				}

				double d = value.doubleValue();
				return d >= lower && d < upper;
			}
		};
	}

	/**
	 * Get a condition that will match if a probed value is above the given
	 * threshold.
	 *
	 * @param value
	 * @return
	 */
	public static <T extends Number> Condition<T> above(final double threshold)
	{
		return new Condition<T>()
		{
			public boolean matches(T value)
			{
				return value.doubleValue() > threshold;
			}

			@Override
			public String toString()
			{
				return "is above " + threshold;
			}
		};
	}

	/**
	 * Get a condition that will match if a probed value is below the given
	 * threshold.
	 *
	 * @param value
	 * @return
	 */
	public static <T extends Number> Condition<T> below(final double threshold)
	{
		return new Condition<T>()
		{
			public boolean matches(T value)
			{
				return value.doubleValue() < threshold;
			}

			@Override
			public String toString()
			{
				return "is below " + threshold;
			}
		};
	}
}
