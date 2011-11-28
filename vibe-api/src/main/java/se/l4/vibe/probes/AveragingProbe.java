package se.l4.vibe.probes;


/**
 * Probe that calculates the average of another probe.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class AveragingProbe<T extends Number>
	extends AbstractSampledProbe<Double>
{
	private long accumulated;
	private long samples;
	private final SampledProbe<T> probe;

	public AveragingProbe(SampledProbe<T> probe)
	{
		this.probe = probe;
	}

	@Override
	public Double peek()
	{
		long diff = probe.peek().longValue();
		return (accumulated + diff) / (samples + 1.0);
	}

	@Override
	protected Double sample0()
	{
		accumulated += probe.read().longValue(); 
		samples++;
		return accumulated / (double) samples;
	}

}
