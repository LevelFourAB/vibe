package se.l4.vibe.checks;

import java.util.Objects;
import java.util.function.Predicate;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

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
	@NonNull
	public static <T> Predicate<T> not(@NonNull Predicate<T> instance)
	{
		return instance.negate();
	}


	/**
	 * Check that the value is equal to the given instance.
	 *
	 * @param instance
	 * @return
	 */
	@NonNull
	public static <T> Predicate<T> is(@Nullable T instance)
	{
		return v -> Objects.equals(v, instance);
	}

	/**
	 * Check that a value is equal to the specified number.
	 *
	 * @param number
	 * @return
	 */
	@NonNull
	public static Predicate<Number> is(double number)
	{
		return v -> v != null && v.doubleValue() == number;
	}

	/**
	 * Check that a value is equal to the specified number.
	 *
	 * @param number
	 * @return
	 */
	@NonNull
	public static Predicate<Number> is(long number)
	{
		return v -> v != null && v.longValue() == number;
	}

	/**
	 * Check that a value is within the given range. Lower is inclusive,
	 * while upper is exclusive.
	 *
	 * @param number
	 * @return
	 */
	@NonNull
	public static Predicate<Number> inRange(double lower, double upper)
	{
		return v -> {
			if(v == null) return false;

			double d = v.doubleValue();
			return d >= lower && d < upper;
		};
	}

	/**
	 * Get a condition that will match if a probed value is above the given
	 * threshold.
	 *
	 * @param value
	 * @return
	 */
	@NonNull
	public static <T extends Number> Predicate<T> above(double threshold)
	{
		return v -> v != null && v.doubleValue() > threshold;
	}

	/**
	 * Get a condition that will match if a probed value is below the given
	 * threshold.
	 *
	 * @param value
	 * @return
	 */
	@NonNull
	public static <T extends Number> Predicate<T> below(double threshold)
	{
		return v -> v != null && v.doubleValue() < threshold;
	}
}
