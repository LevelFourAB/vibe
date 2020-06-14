package se.l4.vibe.sampling;

import edu.umd.cs.findbugs.annotations.NonNull;

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
	void sampleAcquired(@NonNull Sample<T> sample);
}
