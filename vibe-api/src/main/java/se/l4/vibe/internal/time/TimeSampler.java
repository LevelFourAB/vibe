package se.l4.vibe.internal.time;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.internal.SampleTime;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.probes.TimeSeries;
import se.l4.vibe.probes.TimeSeriesSampler;


public class TimeSampler
{
	private final Map<SampleTime, TimeSeriesSampler> samplers;
	private final ScheduledExecutorService executor;
	private final SampleTime defaultTime;
	
	public TimeSampler(SampleTime defaultTime)
	{
		this.defaultTime = defaultTime;
		
		samplers = new HashMap<SampleTime, TimeSeriesSampler>();
		
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
	
	public SampleTime getDefaultTime()
	{
		return defaultTime;
	}

	public <T> TimeSeries<T> sampleTimeSeries(SampleTime time, SampledProbe<T> probe)
	{
		TimeSeriesSampler sampler = samplers.get(time);
		if(sampler == null)
		{
			sampler = new TimeSeriesSampler(executor, time.getInterval(), time.getRetention(), TimeUnit.MILLISECONDS);
			samplers.put(time, sampler);
			sampler.start();
		}
		
		// Create the series
		return sampler.add(probe);
	}
	
}
