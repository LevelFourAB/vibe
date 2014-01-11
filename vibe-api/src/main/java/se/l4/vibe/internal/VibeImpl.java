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
import se.l4.vibe.builder.TimeSeriesBuilder;
import se.l4.vibe.event.Events;
import se.l4.vibe.internal.builder.EventsBuilderImpl;
import se.l4.vibe.internal.builder.ProbeBuilderImpl;
import se.l4.vibe.internal.builder.TimeSeriesBuilderImpl;
import se.l4.vibe.internal.time.TimeSampler;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.probes.TimeSeries;

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
	public VibeImpl(VibeBackend backend, long sampleInterval, long sampleRetention)
	{
		instances = new ConcurrentHashMap<String, Object>();
		
		this.backend = new BackendImpl();
		if(backend != null)
		{
			this.backend.add(backend);
		}
		
		sampler = new TimeSampler(new SampleTime(sampleInterval, sampleRetention));
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
	public <T> TimeSeriesBuilder<T> timeSeries(SampledProbe<T> probe)
	{
		return new TimeSeriesBuilderImpl<T>(backend, sampler, probe);
	}
	
	@Override
	public <T> EventsBuilder<T> events(Class<T> base)
	{
		return new EventsBuilderImpl<T>(backend);
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
	public <T> TimeSeries<T> getTimeSeries(String path)
	{
		Object o = instances.get(path);
		return o instanceof TimeSeries ? (TimeSeries) o : null;
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
		public void export(String path, TimeSeries<?> series)
		{
			checkPathAndAdd(path, series);
			for(VibeBackend vb : backends)
			{
				vb.export(path, series);
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
				else if(o instanceof TimeSeries)
				{
					backend.export(path, (TimeSeries) o);
				}
			}
		}
	}
}
