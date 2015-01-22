package se.l4.vibe.internal.time;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.internal.SampleCollector;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.probes.Sampler;


public class TimeSampler
{
	private final Map<Long, SampleCollector> samplers;
	private final ScheduledExecutorService executor;
	private final long defaultTime;
	
	public TimeSampler(long defaultTime)
	{
		this.defaultTime = defaultTime;
		
		samplers = new HashMap<>();
		
		executor = Executors.newScheduledThreadPool(1, new ThreadFactory()
		{
			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread(r, "vibe");
				t.setDaemon(true);
				return t;
			}
		});
	}
	
	public long getDefaultTime()
	{
		return defaultTime;
	}

	public <T> Sampler<T> sampleTimeSeries(long sampleIntervalInMs, SampledProbe<T> probe)
	{
		SampleCollector sampler = samplers.get(sampleIntervalInMs);
		if(sampler == null)
		{
			sampler = new SampleCollector(executor, sampleIntervalInMs, TimeUnit.MILLISECONDS);
			samplers.put(sampleIntervalInMs, sampler);
			sampler.start();
		}
		
		// Create the series
		return sampler.add(probe);
	}
	
}
