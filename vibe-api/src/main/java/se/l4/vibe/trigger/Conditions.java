package se.l4.vibe.trigger;

/**
 * Conditions available for triggers.
 * 
 * @author Andreas Holstenson
 *
 */
public class Conditions
{
	private Conditions()
	{
	}
	
	/**
	 * Get a condition that will match if a probed value is above the given
	 * threshold.
	 * 
	 * @param value
	 * @return
	 */
	public static <T extends Number> Condition<T> above(final T threshold)
	{
		return new Condition<T>()
		{
			public boolean matches(T value)
			{
				return value.doubleValue() > threshold.doubleValue();
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
	public static <T extends Number> Condition<T> below(final T threshold)
	{
		return new Condition<T>()
		{
			public boolean matches(T value)
			{
				return value.doubleValue() < threshold.doubleValue();
			}
			
			@Override
			public String toString()
			{
				return "is below " + threshold;
			}
		};
	}
}
