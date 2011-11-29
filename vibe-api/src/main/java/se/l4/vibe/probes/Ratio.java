package se.l4.vibe.probes;

/**
 * Probe that provides a ratio between two values.
 * 
 * @author Andreas Holstenson
 *
 */
public class Ratio
	extends AbstractSampledProbe<Double>
{
	private final SampledProbe<? extends Number> probe1;
	private final SampledProbe<? extends Number> probe2;

	public Ratio(SampledProbe<? extends Number> probe1,
			SampledProbe<? extends Number> probe2)
	{
		this.probe1 = probe1;
		this.probe2 = probe2;
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
	public static Ratio between(
			SampledProbe<? extends Number> a,
			SampledProbe<? extends Number> b)
	{
		return new Ratio(a, b);
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
	public static Ratio between1(
			SampledProbe<? extends Number> a,
			double b)
	{
		return new Ratio(a, new ConstantProbe<Double>(b));
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
	public static Ratio between(
			double a, 
			SampledProbe<? extends Number> b)
	{
		return new Ratio(new ConstantProbe<Double>(a), b);
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
