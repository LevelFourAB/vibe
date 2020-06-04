package se.l4.vibe.probes;

/**
 * Probe that can measure a certain value.
 *
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface Probe<T>
{
	/**
	 * Read the value.
	 *
	 * @return
	 */
	T read();
}
