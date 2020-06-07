package se.l4.vibe.internal;

import java.util.HashSet;
import java.util.Set;

import se.l4.vibe.Export;
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

	private final Set<Export<?>> exports;

	public ScopedVibe(
		VibeImpl vibe,
		VibeBackend parent,
		String scope
	)
	{
		this.vibe = vibe;
		this.parent = parent;
		this.scope = scope;

		this.exports = new HashSet<>();
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
		return vibe.export(scope, object, this::mapExport);
	}

	@Override
	public void destroy()
	{
		for(Export<?> export : exports)
		{
			export.remove();
		}
	}

	private <T extends Metric> Export<T> mapExport(Export<T> export)
	{
		exports.add(export);

		return new Export<T>()
		{
			@Override
			public T get()
			{
				return export.get();
			}

			@Override
			public void remove()
			{
				exports.remove(export);
				export.remove();
			}
		};
	}
}
