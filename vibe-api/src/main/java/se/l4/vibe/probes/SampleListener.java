package se.l4.vibe.probes;

/**
 * Listener for sample events.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface SampleListener<T>
{
	/**
	 * A sample has been acquired from the probe.
	 * 
	 * @param probe
	 * @param entry
	 */
	void sampleAcquired(SampledProbe<T> probe, TimeSeries.Entry<T> entry);
}
