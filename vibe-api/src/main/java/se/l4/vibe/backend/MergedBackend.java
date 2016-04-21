package se.l4.vibe.backend;

import se.l4.vibe.event.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.Sampler;
import se.l4.vibe.timer.Timer;

/**
 * Collection of several backends that are triggered in order.
 * 
 * @author Andreas Holstenson
 *
 */
public class MergedBackend
	implements VibeBackend
{
	private final VibeBackend[] backends;

	public MergedBackend(VibeBackend... backends)
	{
		this.backends = backends;
	}

	@Override
	public void export(String path, Sampler<?> series)
	{
		for(VibeBackend backend : backends)
		{
			backend.export(path, series);
		}
	}

	@Override
	public void export(String path, Probe<?> probe)
	{
		for(VibeBackend backend : backends)
		{
			backend.export(path, probe);
		}
	}

	@Override
	public void export(String path, Events<?> events)
	{
		for(VibeBackend backend : backends)
		{
			backend.export(path, events);
		}
	}
	
	@Override
	public void export(String path, Timer timer)
	{
		for(VibeBackend backend : backends)
		{
			backend.export(path, timer);
		}
	}
	
	@Override
	public void close()
	{
		for(VibeBackend backend : backends)
		{
			backend.close();
		}
	}
}
