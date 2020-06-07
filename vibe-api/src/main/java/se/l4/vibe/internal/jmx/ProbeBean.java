package se.l4.vibe.internal.jmx;

import java.util.Collection;
import java.util.Collections;

import se.l4.vibe.probes.Probe;

/**
 * Wrapper for {@link Probe} to treat it as a service.
 *
 * @author Andreas Holstenson
 *
 */
public class ProbeBean
	implements JmxExport
{
	private final Probe<?> probe;

	public ProbeBean(Probe<?> probe)
	{
		this.probe = probe;
	}

	@Override
	public Collection<Attribute> getAttributes()
	{
		return Collections.singleton(
			new Attribute("currentValue", Object.class)
		);
	}

	@Override
	public Object getAttribute(String attribute)
	{
		switch(attribute)
		{
			case "currentValue":
				return probe.read();
		}

		return null;
	}
}
