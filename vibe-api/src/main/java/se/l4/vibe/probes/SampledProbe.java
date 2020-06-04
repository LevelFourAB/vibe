package se.l4.vibe.probes;

/**
 * Probe that measures a value that is sampled at certain intervals. A call
 * to a {@link #read()} on a sampled probe should return the <i>last</i> value
 * returned by {@link #sample()}.
 *
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface SampledProbe<T>
	extends Probe<T>
{
	/**
	 * Check what the current value is.
	 *
	 * @return
	 */
	T peek();

	/**
	 * Sample the current value and optionally reset the probe.
	 *
	 * @return
	 */
	T sample();
}
