package se.l4.vibe.operations;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Operations for scaling values.
 */
public class Scale
{
	private Scale()
	{
	}

	/**
	 * Create an operation that will scale numbers to the given number of
	 * decimals.
	 *
	 * @param decimals
	 * @return
	 */
	@NonNull
	public static Operation<? extends Number, Double> scale(
		int decimals
	)
	{
		double scale = decimals * 10;
		return () -> in -> (Math.round(in.doubleValue() * scale)) / (double) scale;
	}


	/**
	 * Create an operation that will divide a value with the given divisor and
	 * return it as a double.
	 *
	 * @param divisor
	 * @return
	 */
	@NonNull
	public static Operation<? extends Number, Double> divide(
		double divisor
	)
	{
		return () -> in -> in.doubleValue() / divisor;
	}

	/**
	 * Create an operation that will divide a value with the given divisor and
	 * return it as a long.
	 *
	 * @param divisor
	 * @return
	 */
	@NonNull
	public static Operation<? extends Number, Long> divideAsLong(
		double divisor
	)
	{
		return () -> in -> (long) (in.longValue() / divisor);
	}

	/**
	 * Create an operation that will multiply a value with the given multiplier
	 * and return it as a double.
	 *
	 * @param multiplier
	 * @return
	 */
	@NonNull
	public static Operation<? extends Number, Double> multiply(
		double multiplier
	)
	{
		return () -> in -> in.doubleValue() * multiplier;
	}

	/**
	 * Create an operation that will multiply a value with the given multiplier
	 * and return it as a long.
	 *
	 * @param multiplier
	 * @return
	 */
	@NonNull
	public static Operation<? extends Number, Long> multiplyAsLong(
		double multiplier
	)
	{
		return () -> in -> (long) (in.doubleValue() * multiplier);
	}
}
