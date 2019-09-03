package se.l4.vibe.internal;

import se.l4.vibe.Vibe;
import se.l4.vibe.backend.VibeBackend;
import se.l4.vibe.builder.EventsBuilder;
import se.l4.vibe.builder.ProbeBuilder;
import se.l4.vibe.builder.SamplerBuilder;
import se.l4.vibe.builder.TimerBuilder;
import se.l4.vibe.event.Events;
import se.l4.vibe.internal.builder.EventsBuilderImpl;
import se.l4.vibe.internal.builder.ProbeBuilderImpl;
import se.l4.vibe.internal.builder.SamplerBuilderImpl;
import se.l4.vibe.internal.builder.TimerBuilderImpl;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.probes.Sampler;
import se.l4.vibe.timer.Timer;

public class ScopedVibe
	implements Vibe
{
	private final VibeImpl vibe;
	private final VibeBackend parent;
	private final String scope;
	
	private final ScopedBackend backend;

	public ScopedVibe(VibeImpl vibe, VibeBackend parent, String scope)
	{
		this.vibe = vibe;
		this.parent = parent;
		this.scope = scope;
		
		this.backend = new ScopedBackend();
	}
	
	private String scopePath(String path)
	{
		return scope + '/' + path;
	}
	
	@Override
	public <T> ProbeBuilder<T> probe(Probe<T> probe)
	{
		return new ProbeBuilderImpl<>(backend, probe);
	}

	@Override
	public <T> SamplerBuilder<T> sample(SampledProbe<T> probe)
	{
		return new SamplerBuilderImpl<>(backend, vibe.sampler(), probe);
	}

	@Override
	public <T> EventsBuilder<T> events(Class<T> base)
	{
		return new EventsBuilderImpl<>(backend);
	}

	@Override
	public TimerBuilder timer()
	{
		return new TimerBuilderImpl(backend);
	}

	@Override
	public <T> Probe<T> getProbe(String path)
	{
		return vibe.getProbe(scopePath(path));
	}

	@Override
	public <T> Sampler<T> getTimeSeries(String path)
	{
		return vibe.getTimeSeries(scopePath(path));
	}

	@Override
	public <T> Events<T> getEvents(String path)
	{
		return vibe.getEvents(scopePath(path));
	}

	@Override
	public Timer getTimer(String path)
	{
		return vibe.getTimer(scopePath(path));
	}

	@Override
	public Vibe scope(String path)
	{
		return new ScopedVibe(vibe, parent, scopePath(path));
	}

	@Override
	public void addBackend(VibeBackend backend)
	{
		throw new UnsupportedOperationException();
	}

	private class ScopedBackend
		implements VibeBackend
	{
		public ScopedBackend()
		{
		}
		
		@Override
		public void export(String path, Events<?> events)
		{
			parent.export(scopePath(path), events);
		}
		
		@Override
		public void export(String path, Probe<?> probe)
		{
			parent.export(scopePath(path), probe);
		}
		
		@Override
		public void export(String path, Sampler<?> series)
		{
			parent.export(scopePath(path), series);
		}
		
		@Override
		public void export(String path, Timer timer)
		{
			parent.export(scopePath(path), timer);
		}
		
		@Override
		public void close()
		{
			// Closing scoped instances does nothing
		}
	}

	@Override
	public void destroy()
	{
		// Destroying the scoped instance does nothing
	}
}
