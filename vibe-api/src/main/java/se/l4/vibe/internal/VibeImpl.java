package se.l4.vibe.internal;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import se.l4.vibe.Export;
import se.l4.vibe.ExportBuilder;
import se.l4.vibe.Metric;
import se.l4.vibe.Vibe;
import se.l4.vibe.VibeException;
import se.l4.vibe.backend.MergedBackend;
import se.l4.vibe.backend.VibeBackend;
import se.l4.vibe.event.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.SampledProbe;
import se.l4.vibe.sampling.Sampler;
import se.l4.vibe.timer.Timer;

/**
 * Implementation of {@link Vibe}.
 */
public class VibeImpl
	implements Vibe
{
	private final Map<String, VibeBackend.Handle> exported;
	private final VibeBackend backend;

	/**
	 * Create a new instance.
	 *
	 * @param backends
	 * 	backends that should be used when exporting metrics
	 */
	public VibeImpl(
		VibeBackend[] backends
	)
	{
		exported = new ConcurrentHashMap<>();

		this.backend = new MergedBackend(backends);
	}

	@Override
	public Vibe scope(String path)
	{
		return new ScopedVibe(this, backend, path);
	}

	@Override
	public <T extends Metric> ExportBuilder<T> export(T object)
	{
		return export(null, object, null);
	}

	public <T extends Metric> ExportBuilder<T> export(
		String path0,
		T object,
		Function<Export<T>, Export<T>> exportMapper
	)
	{
		return new ExportBuilder<T>()
		{
			protected String path = path0;

			protected String mergePath(String nextPath)
			{
				nextPath = nextPath.trim();
				if(nextPath.isEmpty())
				{
					// Skip empty path segments
					return path;
				}

				if(path == null)
				{
					return nextPath;
				}
				else
				{
					return path + '/' + nextPath;
				}
			}

			@Override
			public ExportBuilder<T> at(String path)
			{
				this.path = mergePath(path);
				return this;
			}

			@Override
			public ExportBuilder<T> at(String... hierarchy)
			{
				StringBuilder path = new StringBuilder();
				for(int i=0, n=hierarchy.length; i<n; i++)
				{
					if(i > 0) path.append('/');

					String segment = hierarchy[i];
					if(segment.indexOf('/') != -1)
					{
						throw new IllegalArgumentException("Segments may not contain /; For " + segment);
					}

					path.append(segment);
				}

				this.path = mergePath(path.toString());
				return this;
			}

			@Override
			public ExportBuilder<T> at(Class<?> type)
			{
				Objects.requireNonNull(type, "type can not be null");

				this.path = mergePath(type.getName().replace('.', '/'));
				return this;
			}

			@Override
			public Export<T> done()
			{
				Export<T> result = exportObject(path, object);

				if(exportMapper != null)
				{
					return exportMapper.apply(result);
				}
				else
				{
					return result;
				}
			}
		};
	}

	@Override
	public void destroy()
	{
		// Remove all of the handles
		for(VibeBackend.Handle handle : exported.values())
		{
			handle.remove();
		}

		// Ask the backends to stop
		this.backend.close();

		// Clear the exported objects
		exported.clear();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <T extends Metric> Export<T> exportObject(String path, T object)
	{
		if(exported.containsKey(path))
		{
			throw new VibeException("path is already registered: " + path);
		}

		VibeBackend.Handle handle;
		if(object instanceof Sampler)
		{
			handle = backend.export(path, (Sampler) object);
		}
		else if(object instanceof Timer)
		{
			handle = backend.export(path, (Timer) object);
		}
		else if(object instanceof Events)
		{
			handle = backend.export(path, (Events) object);
		}
		else if(object instanceof SampledProbe)
		{
			Sampler<?> sampler = Sampler.forProbe((SampledProbe) object)
				.build();

			handle = backend.export(path, sampler);
		}
		else if(object instanceof Probe)
		{
			handle = backend.export(path, (Probe) object);
		}
		else
		{
			throw new VibeException("Unsupported type of object: " + object.getClass().getName());
		}

		exported.put(path, handle);

		return new Export<T>()
		{
			@Override
			public T get()
			{
				return object;
			}

			@Override
			public void remove()
			{
				handle.remove();
				exported.remove(path);
			}
		};
	}

	public static class BuilderImpl
		implements Builder
	{
		private final List<VibeBackend> backends;

		public BuilderImpl()
		{
			backends = new ArrayList<>();
		}

		@Override
		public Builder withBackend(VibeBackend backend)
		{
			Objects.requireNonNull(backend, "backend must not be null");

			backends.add(backend);
			return this;
		}

		public Vibe build()
		{
			return new VibeImpl(
				backends.toArray(new VibeBackend[backends.size()])
			);
		}
	}
}
