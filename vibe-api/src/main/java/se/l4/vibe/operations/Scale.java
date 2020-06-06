package se.l4.vibe.operations;

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
	public static SampleAndProbeOperation<? extends Number, Double> scale(
		int decimals
	)
	{
		double scale = decimals * 10;
		return in -> (Math.round(in.doubleValue() * scale)) / (double) scale;
	}


	/**
	 * Create an operation that will divide a value with the given divisor and
	 * return it as a double.
	 *
	 * @param divisor
	 * @return
	 */
	public static SampleAndProbeOperation<? extends Number, Double> divide(
		double divisor
	)
	{
		return in -> in.doubleValue() / divisor;
	}

	/**
	 * Create an operation that will divide a value with the given divisor and
	 * return it as a long.
	 *
	 * @param divisor
	 * @return
	 */
	public static SampleAndProbeOperation<? extends Number, Long> divideAsLong(
		double divisor
	)
	{
		return in -> (long) (in.longValue() / divisor);
	}

	/**
	 * Create an operation that will multiply a value with the given multiplier
	 * and return it as a double.
	 *
	 * @param multiplier
	 * @return
	 */
	public static SampleAndProbeOperation<? extends Number, Double> multiply(
		double multiplier
	)
	{
		return in -> in.doubleValue() * multiplier;
	}

	/**
	 * Create an operation that will multiply a value with the given multiplier
	 * and return it as a long.
	 *
	 * @param multiplier
	 * @return
	 */
	public static SampleAndProbeOperation<? extends Number, Long> multiplyAsLong(
		double multiplier
	)
	{
		return in -> (long) (in.doubleValue() * multiplier);
	}
}
