package se.l4.vibe.sampling;

/**
 * Listener for sample events.
 *
 * @param <T>
 */
public interface SampleListener<T>
{
	/**
	 * A sample has been acquired from the probe.
	 *
	 * @param sample
	 */
	void sampleAcquired(Sample<T> sample);
}
