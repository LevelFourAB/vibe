package se.l4.vibe.internal;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import se.l4.vibe.Export;
import se.l4.vibe.ExportBuilder;
import se.l4.vibe.Exportable;
import se.l4.vibe.Handle;
import se.l4.vibe.Vibe;
import se.l4.vibe.VibeBackend;
import se.l4.vibe.VibeException;
import se.l4.vibe.VibePaths;
import se.l4.vibe.checks.Check;
import se.l4.vibe.events.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.SampledProbe;
import se.l4.vibe.sampling.TimeSampler;
import se.l4.vibe.timers.Timer;

/**
 * Implementation of {@link Vibe}.
 */
public class VibeImpl
	implements Vibe
{
	private final Map<String, Handle> exported;
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
	public Vibe scope(String... hierarchy)
	{
		return new ScopedVibe(this, backend, VibePaths.hierarchy(hierarchy));
	}

	@Override
	public <T extends Exportable> ExportBuilder<T> export(T object)
	{
		return export(null, object, null);
	}

	public <T extends Exportable> ExportBuilder<T> export(
		String path0,
		T object,
		Function<Export<T>, Export<T>> exportMapper
	)
	{
		return new ExportBuilder<T>()
		{
			protected String path = path0;

			@Override
			public ExportBuilder<T> at(String path)
			{
				this.path = VibePaths.merge(this.path, path);
				return this;
			}

			@Override
			public ExportBuilder<T> at(String... hierarchy)
			{
				this.path = VibePaths.merge(
					this.path,
					VibePaths.hierarchy(hierarchy)
				);
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
		for(Handle handle : exported.values())
		{
			handle.release();
		}

		// Ask the backends to stop
		this.backend.close();

		// Clear the exported objects
		exported.clear();
	}

	@SuppressWarnings({ "rawtypes" })
	protected <T extends Exportable> Export<T> exportObject(String path, T object)
	{
		if(exported.containsKey(path))
		{
			throw new VibeException("path is already registered: " + path);
		}

		Handle handle;
		if(object instanceof TimeSampler)
		{
			handle = backend.export(path, (TimeSampler) object);
		}
		else if(object instanceof Timer)
		{
			handle = backend.export(path, (Timer) object);
		}
		else if(object instanceof Events)
		{
			handle = backend.export(path, (Events) object);
		}
		else if(object instanceof Check)
		{
			handle = backend.export(path, (Check) object);
		}
		else if(object instanceof SampledProbe)
		{
			handle = backend.export(path, (SampledProbe) object);
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
				handle.release();
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
		public Builder addBackend(VibeBackend backend)
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
