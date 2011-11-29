package se.l4.vibe.probes;

/**
 * Methods for scaling probe values. Useful for example when wanting to measure
 * data in kilobytes instead of bytes.
 * 
 * @author Andreas Holstenson
 *
 */
public class Scale
{
	private Scale()
	{
	}
	
	/**
	 * Scale a number to a number of significant decimals.
	 * 
	 * @param probe
	 * @param decimals
	 * @return
	 */
	public static SampledProbe<Double> scale(
			SampledProbe<? extends Number> probe,
			int decimals)
	{
		return new ScaledSampledProbe(probe, decimals);
	}
	
	/**
	 * Scale a number to a number of significant decimals.
	 * 
	 * @param probe
	 * @param decimals
	 * @return
	 */
	public static Probe<Double> scale(
			Probe<? extends Number> probe,
			int decimals)
	{
		return new ScaledProbe(probe, decimals);
	}
	
	/**
	 * Create a new sampled probe that will use another probe.
	 * 
	 * @param probe
	 * @param divisor
	 * @return
	 */
	public static SampledProbe<Double> divide(
			SampledProbe<? extends Number> probe,
			int divisor)
	{
		return new DoubleSampledProbe(probe, 1.0 / divisor);
	}
	
	/**
	 * Create a new sampled probe that will use another probe.
	 * 
	 * @param probe
	 * @param divisor
	 * @return
	 */
	public static SampledProbe<Long> divideAsLong(
			SampledProbe<? extends Number> probe,
			int divisor)
	{
		return new LongSampledProbe(probe, 1.0 / divisor);
	}
	
	/**
	 * Create a new sampled probe that will use another probe.
	 * 
	 * @param probe
	 * @param divisor
	 * @return
	 */
	public static SampledProbe<Double> multiply(
			SampledProbe<? extends Number> probe,
			int multiplier)
	{
		return new DoubleSampledProbe(probe, multiplier);
	}
	
	/**
	 * Create a new sampled probe that will use another probe.
	 * 
	 * @param probe
	 * @param divisor
	 * @return
	 */
	public static SampledProbe<Long> multiplyAsLong(
			SampledProbe<? extends Number> probe,
			int multiplier)
	{
		return new LongSampledProbe(probe, 1.0 / multiplier);
	}
	
	/**
	 * Create a new probe that will divide the value of another probe.
	 * 
	 * @param probe
	 * @param divisor
	 * @return
	 */
	public static Probe<Double> divide(
			Probe<? extends Number> probe,
			int divisor)
	{
		return new DoubleProbe(probe, 1.0 / divisor);
	}
	
	/**
	 * Create a new probe that will divide the value of another probe.
	 * 
	 * @param probe
	 * @param divisor
	 * @return
	 */
	public static Probe<Long> divideAsLong(
			Probe<? extends Number> probe,
			int divisor)
	{
		return new LongProbe(probe, 1.0 / divisor);
	}
	
	/**
	 * Create a new probe that will multiply the value of another probe.
	 * 
	 * @param probe
	 * @param divisor
	 * @return
	 */
	public static Probe<Double> multiply(
			Probe<? extends Number> probe,
			int multiplier)
	{
		return new DoubleProbe(probe, multiplier);
	}
	
	/**
	 * Create a new that will multiply the value of another probe.
	 * 
	 * @param probe
	 * @param divisor
	 * @return
	 */
	public static Probe<Long> multiplyAsLong(
			Probe<? extends Number> probe,
			int multiplier)
	{
		return new LongProbe(probe, 1.0 / multiplier);
	}
	
	private static class ScaledSampledProbe
		implements SampledProbe<Double>
	{
		private final SampledProbe<? extends Number> probe;
		private final int scale;

		public ScaledSampledProbe(SampledProbe<? extends Number> probe, int scale)
		{
			this.probe = probe;
			this.scale = scale * 10;
		}
		
		private double scale(Number in)
		{
			return (Math.round(in.doubleValue() * scale)) / (double) scale;
		}
		
		@Override
		public Double peek()
		{
			return scale(probe.peek());
		}
		
		@Override
		public Double read()
		{
			return scale(probe.read());
		}
		
		@Override
		public Double sample()
		{
			return scale(probe.sample());
		}
	}
	
	private static class ScaledProbe
		implements Probe<Double>
	{
		private final Probe<? extends Number> probe;
		private final int scale;
	
		public ScaledProbe(Probe<? extends Number> probe, int scale)
		{
			this.probe = probe;
			this.scale = scale * 10;
		}
		
		private double scale(Number in)
		{
			return (Math.round(in.doubleValue() * scale)) / (double) scale;
		}
		
		@Override
		public Double read()
		{
			return scale(probe.read());
		}
	}
	
	private static class DoubleSampledProbe
		implements SampledProbe<Double>
	{
		private final SampledProbe<? extends Number> probe;
		private final double scale;

		public DoubleSampledProbe(SampledProbe<? extends Number> probe, double scale)
		{
			this.probe = probe;
			this.scale = scale;
		}
		
		@Override
		public Double peek()
		{
			return probe.peek().doubleValue() * scale;
		}
		
		@Override
		public Double read()
		{
			return probe.read().doubleValue() * scale;
		}
		
		@Override
		public Double sample()
		{
			return probe.sample().doubleValue() * scale;
		}
	}
	
	private static class LongSampledProbe
		implements SampledProbe<Long>
	{
		private final SampledProbe<? extends Number> probe;
		private final double scale;
	
		public LongSampledProbe(SampledProbe<? extends Number> probe, double scale)
		{
			this.probe = probe;
			this.scale = scale;
		}
		
		@Override
		public Long peek()
		{
			return (long) (probe.peek().doubleValue() * scale);
		}
		
		@Override
		public Long read()
		{
			return (long) (probe.read().doubleValue() * scale);
		}
		
		@Override
		public Long sample()
		{
			return (long) (probe.sample().doubleValue() * scale);
		}
	}
	
	private static class DoubleProbe
		implements Probe<Double>
	{
		private final Probe<? extends Number> probe;
		private final double scale;
	
		public DoubleProbe(Probe<? extends Number> probe, double scale)
		{
			this.probe = probe;
			this.scale = scale;
		}
		
		@Override
		public Double read()
		{
			return probe.read().doubleValue() * scale;
		}
	}
	
	private static class LongProbe
		implements Probe<Long>
	{
		private final Probe<? extends Number> probe;
		private final double scale;
	
		public LongProbe(Probe<? extends Number> probe, double scale)
		{
			this.probe = probe;
			this.scale = scale;
		}
		
		@Override
		public Long read()
		{
			return (long) (probe.read().doubleValue() * scale);
		}
	}
}
