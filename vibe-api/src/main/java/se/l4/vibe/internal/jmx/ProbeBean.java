package se.l4.vibe.internal.jmx;

import se.l4.vibe.internal.service.ExposeAsAttribute;
import se.l4.vibe.probes.Probe;

/**
 * Wrapper for {@link Probe} to treat it as a service.
 *
 * @author Andreas Holstenson
 *
 */
public class ProbeBean
{
	private final Probe<?> probe;

	public ProbeBean(Probe<?> probe)
	{
		this.probe = probe;
	}

	@ExposeAsAttribute
	public Object get()
	{
		return probe.read();
	}
}
