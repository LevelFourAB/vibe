package se.l4.vibe.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.Vibe;
import se.l4.vibe.backend.VibeBackend;
import se.l4.vibe.event.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.probes.TimeSeries;
import se.l4.vibe.probes.TimeSeriesSampler;

/**
 * Implementation of {@link Vibe}.
 * 
 * @author Andreas Holstenson
 *
 */
public class DefaultVibe
	implements Vibe
{
	private final VibeBackend backend;
	private final long defaultSampleInterval;
	private final long defaultSampleRetention;
	
	private final Map<SampleTime, TimeSeriesSampler> samplers;

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
	public DefaultVibe(VibeBackend backend, long sampleInterval, long sampleRetention)
	{
		this.backend = backend;
		this.defaultSampleInterval = sampleInterval;
		this.defaultSampleRetention = sampleRetention;
		
		samplers = new HashMap<SampleTime, TimeSeriesSampler>();
	}

	@Override
	public <T> ProbeBuilder<T> probe(Probe<T> probe)
	{
		return new ProbeBuilderImpl<T>(probe);
	}

	@Override
	public <T> TimeSeriesBuilder<T> timeSeries(SampledProbe<T> probe)
	{
		return new TimeSeriesBuilderImpl<T>(probe);
	}
	
	@Override
	public <T> EventsBuilder<T> events(Class<T> base)
	{
		return new EventsBuilderImpl<T>();
	}
	
	private static class AbstractBuilder<Self>
		implements Builder<Self>
	{
		protected String path;
		
		@Override
		public Self at(String path)
		{
			this.path = path;
			
			return (Self) this;
		}
		
		@Override
		public Self at(String... hierarchy)
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
			
			this.path = path.toString();
			
			return (Self) this;
		}
		
		protected void verify()
		{
			if(path == null)
			{
				throw new IllegalStateException("A path is required");
			}
		}
	}
	
	/**
	 * Builder for time series.
	 * 
	 * @author Andreas Holstenson
	 *
	 * @param <T>
	 */
	private class TimeSeriesBuilderImpl<T>
		extends AbstractBuilder<TimeSeriesBuilder<T>>
		implements TimeSeriesBuilder<T>
	{
		private final SampledProbe<T> probe;
		
		private long sampleInterval;
		private long sampleRetention;
		
		public TimeSeriesBuilderImpl(SampledProbe<T> probe)
		{
			this.probe = probe;
			
			sampleInterval = defaultSampleInterval;
			sampleRetention = defaultSampleRetention;
		}
		
		@Override
		public TimeSeries<T> export()
		{
			verify();
			
			// Find or create a suitable sampler
			SampleTime time = new SampleTime(sampleInterval, sampleRetention);
			TimeSeriesSampler sampler = samplers.get(time);
			if(sampler == null)
			{
				sampler = new TimeSeriesSampler(sampleInterval, sampleRetention, TimeUnit.MILLISECONDS);
				samplers.put(time, sampler);
				sampler.start();
			}
			
			TimeSeries<T> series = sampler.add(probe);
			backend.export(path, series);
			
			return series;
		}
		
		@Override
		public TimeSeriesBuilder<T> withInterval(long time, TimeUnit unit)
		{
			sampleInterval = unit.toMillis(time);
			return this;
		}
		
		@Override
		public TimeSeriesBuilder<T> withRetention(long time, TimeUnit unit)
		{
			sampleRetention = unit.toMillis(time);
			return this;
		}
	}
	
	/**
	 * Exporter for probes.
	 * 
	 * @author Andreas Holstenson
	 *
	 * @param <T>
	 */
	private class ProbeBuilderImpl<T>
		extends AbstractBuilder<ProbeBuilder<T>>
		implements ProbeBuilder<T>
	{
		private final Probe<T> probe;

		public ProbeBuilderImpl(Probe<T> probe)
		{
			this.probe = probe;
		}
		
		@Override
		public Probe<T> export()
		{
			verify();
			
			backend.export(path, probe);
			
			return null;
		}
	}
	
	private class EventsBuilderImpl<T>
		extends AbstractBuilder<EventsBuilder<T>>
		implements EventsBuilder<T>
	{
		@Override
		public Events<T> create()
		{
			verify();
			
			EventsImpl<T> events = new EventsImpl<T>();
			backend.export(path, events);
			
			return events;
		}
	}
}
