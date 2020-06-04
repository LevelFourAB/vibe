package se.l4.vibe.internal.builder;

import se.l4.vibe.backend.VibeBackend;
import se.l4.vibe.builder.ProbeBuilder;
import se.l4.vibe.probes.Probe;

/**
 * Exporter for probes.
 *
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class ProbeBuilderImpl<T>
	extends AbstractBuilder<ProbeBuilder<T>>
	implements ProbeBuilder<T>
{
	private final VibeBackend backend;
	private final Probe<T> probe;

	public ProbeBuilderImpl(VibeBackend backend, Probe<T> probe)
	{
		this.backend = backend;
		this.probe = probe;
	}

	@Override
	public Probe<T> export()
	{
		verify();

		backend.export(path, probe);

		return probe;
	}
}
