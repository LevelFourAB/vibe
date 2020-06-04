package se.l4.vibe.trigger;

import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.Sampler;

/**
 * Trigger for automatic events. The trigger will create a probe that can
 * be used to check values.
 *
 * @author Andreas Holstenson
 *
 * @param <Input>
 * @param <Output>
 */
public interface Trigger<Input, Output>
{
	/**
	 * Create a probe suitable for use with the specified time series.
	 *
	 * @param series
	 * @return
	 */
	Probe<Output> forSampler(Sampler<Input> series);
}
