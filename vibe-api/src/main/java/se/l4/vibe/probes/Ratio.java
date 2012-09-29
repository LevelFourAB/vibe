package se.l4.vibe.probes;

/**
 * Probe that provides a ratio between two values.
 * 
 * @author Andreas Holstenson
 *
 */
public class Ratio
{
	private Ratio()
	{
	}
	
	/**
	 * Create a new ratio between the two probes. 
	 * 
	 * <p>
	 * Note that the ratio will call {@link SampledProbe#sample()}, which means
	 * that the probes used should not be used as part of a {@link TimeSeries}.
	 * 
	 * @param probe1
	 * @param probe2
	 * @return
	 */
	public static SampledProbe<Double> between(
			SampledProbe<? extends Number> a,
			SampledProbe<? extends Number> b)
	{
		return new SampledRatio(a, b);
	}
	
	/**
	 * Create a ratio between a probe and a static value.
	 * 
	 * <p>
	 * Note that the ratio will call {@link SampledProbe#sample()}, which means
	 * that the probes used should not be used as part of a {@link TimeSeries}.
	 * 
	 * @param probe1
	 * @param number
	 * @return
	 */
	public static SampledProbe<Double> between(
			SampledProbe<? extends Number> a,
			double b)
	{
		return new SampledRatio(a, new ConstantProbe<Double>(b));
	}
	
	/**
	 * Create a ratio between a static value and a probe.
	 * 
	 * <p>
	 * Note that the ratio will call {@link SampledProbe#sample()}, which means
	 * that the probes used should not be used as part of a {@link TimeSeries}.
	 *
	 * @param number
	 * @param probe2
	 * @return
	 */
	public static SampledProbe<Double> between(
			double a, 
			SampledProbe<? extends Number> b)
	{
		return new SampledRatio(new ConstantProbe<Double>(a), b);
	}
	
	/**
	 * Create a new ratio between the two probes. 
	 * 
	 * @param probe1
	 * @param probe2
	 * @return
	 */
	public static Probe<Double> between(
			Probe<? extends Number> a,
			Probe<? extends Number> b)
	{
		return new NormalRatio(a, b);
	}
	
	/**
	 * Create a ratio between a probe and a static value.
	 * 
	 * @param probe1
	 * @param number
	 * @return
	 */
	public static Probe<Double> between(
			Probe<? extends Number> a,
			double b)
	{
		return new NormalRatio(a, new ConstantProbe<Double>(b));
	}
	
	/**
	 * Create a ratio between a static value and a probe.
	 * 
	 * @param number
	 * @param probe2
	 * @return
	 */
	public static Probe<Double> between(
			double a, 
			Probe<? extends Number> b)
	{
		return new NormalRatio(new ConstantProbe<Double>(a), b);
	}
	
	private static class SampledRatio
		extends AbstractSampledProbe<Double>
	{
		private final SampledProbe<? extends Number> probe1;
		private final SampledProbe<? extends Number> probe2;

		public SampledRatio(SampledProbe<? extends Number> probe1,
				SampledProbe<? extends Number> probe2)
		{
			this.probe1 = probe1;
			this.probe2 = probe2;
		}
		
		@Override
		public Double peek()
		{
			return probe1.peek().doubleValue() / probe2.peek().doubleValue();
		}
		
		@Override
		protected Double sample0()
		{
			return probe1.sample().doubleValue() / probe2.sample().doubleValue();
		}
	}
	
	private static class NormalRatio
		implements Probe<Double>
	{
		private final Probe<? extends Number> probe1;
		private final Probe<? extends Number> probe2;
	
		public NormalRatio(Probe<? extends Number> probe1,
				Probe<? extends Number> probe2)
		{
			this.probe1 = probe1;
			this.probe2 = probe2;
		}
		
		@Override
		public Double read()
		{
			return probe1.read().doubleValue() / probe2.read().doubleValue();
		}
	}
}
