package se.l4.vibe.internal;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import se.l4.vibe.Vibe;
import se.l4.vibe.VibeBuilder;
import se.l4.vibe.VibeException;
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
import se.l4.vibe.internal.time.TimeSampler;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.probes.Sampler;
import se.l4.vibe.timer.Timer;

/**
 * Implementation of {@link Vibe}.
 * 
 * @author Andreas Holstenson
 *
 */
public class VibeImpl
	implements Vibe
{
	private final Map<String, Object> instances;
	private final BackendImpl backend;
	private final TimeSampler sampler;

	/**
	 * Create a new instance.
	 * 
	 * @param backend
	 * 		backend to send all built instances to
	 * @param sampleInterval
	 * 		sampling interval in ms
	 * @param sampleRetention
	 * 		sample retention in ms
	 */
	public VibeImpl(VibeBackend backend, long sampleInterval)
	{
		instances = new ConcurrentHashMap<String, Object>();
		
		this.backend = new BackendImpl();
		if(backend != null)
		{
			this.backend.add(backend);
		}
		
		sampler = new TimeSampler(sampleInterval);
	}
	
	/**
	 * Start building a new {@link Vibe}.
	 * 
	 * @return
	 */
	public static VibeBuilder builder()
	{
		return new DefaultVibeBuilder();
	}

	@Override
	public <T> ProbeBuilder<T> probe(Probe<T> probe)
	{
		return new ProbeBuilderImpl<T>(backend, probe);
	}

	@Override
	public <T> SamplerBuilder<T> sample(SampledProbe<T> probe)
	{
		return new SamplerBuilderImpl<T>(backend, sampler, probe);
	}
	
	public TimeSampler sampler()
	{
		return sampler;
	}
	
	@Override
	public <T> EventsBuilder<T> events(Class<T> base)
	{
		return new EventsBuilderImpl<T>(backend);
	}
	
	@Override
	public TimerBuilder timer()
	{
		return new TimerBuilderImpl(backend);
	}
	
	@Override
	public <T> Events<T> getEvents(String path)
	{
		Object o = instances.get(path);
		return o instanceof Events ? (Events) o : null;
	}
	
	@Override
	public <T> Probe<T> getProbe(String path)
	{
		Object o = instances.get(path);
		return o instanceof Probe ? (Probe) o : null;
	}
	
	@Override
	public <T> Sampler<T> getTimeSeries(String path)
	{
		Object o = instances.get(path);
		return o instanceof Sampler ? (Sampler) o : null;
	}
	
	@Override
	public Timer getTimer(String path)
	{
		Object o = instances.get(path);
		return o instanceof Timer ? (Timer) o : null;
	}
	
	@Override
	public Vibe scope(String path)
	{
		return new ScopedVibe(this, backend, path);
	}
	
	@Override
	public void addBackend(VibeBackend backend)
	{
		this.backend.add(backend);
	}
	
	private class BackendImpl
		implements VibeBackend
	{
		private final List<VibeBackend> backends;
		
		public BackendImpl()
		{
			backends = new ArrayList<VibeBackend>();
		}
		
		private void checkPathAndAdd(String path, Object o)
		{
			if(instances.containsKey(path))
			{
				throw new VibeException("Something has already been registered at " + path + ": " + instances.get(path));
			}
			
			instances.put(path, o);
		}
		
		@Override
		public void export(String path, Events<?> events)
		{
			checkPathAndAdd(path, events);
			for(VibeBackend vb : backends)
			{
				vb.export(path, events);
			}
		}
		
		@Override
		public void export(String path, Probe<?> probe)
		{
			checkPathAndAdd(path, probe);
			for(VibeBackend vb : backends)
			{
				vb.export(path, probe);
			}
		}
		
		@Override
		public void export(String path, Sampler<?> series)
		{
			checkPathAndAdd(path, series);
			for(VibeBackend vb : backends)
			{
				vb.export(path, series);
			}
		}
		
		@Override
		public void export(String path, Timer timer)
		{
			checkPathAndAdd(path, timer);
			for(VibeBackend vb : backends)
			{
				vb.export(path, timer);
			}
		}
		
		public void add(VibeBackend backend)
		{
			backends.add(backend);
			for(Map.Entry<String, Object> e : instances.entrySet())
			{
				String path = e.getKey();
				Object o = e.getValue();
				if(o instanceof Events)
				{
					backend.export(path, (Events) o);
				}
				else if(o instanceof Probe)
				{
					backend.export(path, (Probe) o);
				}
				else if(o instanceof Sampler)
				{
					backend.export(path, (Sampler) o);
				}
			}
		}
	}
}
