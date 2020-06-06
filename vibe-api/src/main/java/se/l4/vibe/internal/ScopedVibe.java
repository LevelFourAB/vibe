package se.l4.vibe.internal;

import se.l4.vibe.ExportBuilder;
import se.l4.vibe.Metric;
import se.l4.vibe.Vibe;
import se.l4.vibe.backend.VibeBackend;

/**
 * Instance of {@link Vibe} that is scoped to a certain path.
 */
public class ScopedVibe
	implements Vibe
{
	private final VibeImpl vibe;
	private final VibeBackend parent;
	private final String scope;

	public ScopedVibe(VibeImpl vibe, VibeBackend parent, String scope)
	{
		this.vibe = vibe;
		this.parent = parent;
		this.scope = scope;
	}

	private String scopePath(String path)
	{
		return scope + '/' + path;
	}

	@Override
	public Vibe scope(String path)
	{
		return new ScopedVibe(vibe, parent, scopePath(path));
	}

	@Override
	public <T extends Metric> ExportBuilder<T> export(T object)
	{
		return vibe.export(scope, object);
	}

	@Override
	public void destroy()
	{
		// Destroying the scoped instance does nothing
	}
}
