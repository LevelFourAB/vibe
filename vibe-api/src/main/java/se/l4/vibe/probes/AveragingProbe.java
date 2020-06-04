package se.l4.vibe.probes;

/**
 * Probe similar to {@link CountingProbe} that averages its inputs.
 *
 * @author Andreas Holstenson
 *
 */
public class AveragingProbe
	extends AbstractSampledProbe<Double>
{
	private double value;
	private int samples;

	public AveragingProbe()
	{
	}

	/**
	 * Add a value to this probe.
	 *
	 * @param value
	 */
	public void add(double value)
	{
		value += value;
		samples++;
	}

	@Override
	public Double peek()
	{
		return value / samples;
	}

	@Override
	protected Double sample0()
	{
		double v = value / samples;
		value = 0;
		samples = 0;
		return v;
	}

}
