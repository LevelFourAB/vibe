package se.l4.vibe.internal;


import se.l4.vibe.Vibe;
import se.l4.vibe.VibeBuilder;
import se.l4.vibe.backend.VibeBackend;
import se.l4.vibe.builder.EventsBuilder;
import se.l4.vibe.builder.ProbeBuilder;
import se.l4.vibe.builder.TimeSeriesBuilder;
import se.l4.vibe.internal.builder.EventsBuilderImpl;
import se.l4.vibe.internal.builder.ProbeBuilderImpl;
import se.l4.vibe.internal.builder.TimeSeriesBuilderImpl;
import se.l4.vibe.internal.time.TimeSampler;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampledProbe;

/**
 * Implementation of {@link Vibe}.
 * 
 * @author Andreas Holstenson
 *
 */
public class VibeImpl
	implements Vibe
{
	private final VibeBackend backend;
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
		this.backend = backend;
		
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
}
