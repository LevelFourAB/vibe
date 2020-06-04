package se.l4.vibe.builder;

import se.l4.vibe.probes.Probe;

/**
 * Builder for simple probes.
 *
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface ProbeBuilder<T> extends Builder<ProbeBuilder<T>>
{
	/**
	 * Export and return the probe.
	 *
	 * @return
	 */
	Probe<T> export();
}
