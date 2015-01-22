package se.l4.vibe.internal;

import java.util.concurrent.TimeUnit;

import se.l4.vibe.Vibe;
import se.l4.vibe.VibeBuilder;
import se.l4.vibe.backend.MergedBackend;
import se.l4.vibe.backend.VibeBackend;

/**
 * Builder for {@link Vibe} instances.
 * 
 * @author Andreas Holstenson
 *
 */
public class DefaultVibeBuilder
	implements VibeBuilder
{
	private VibeBackend backend;
	private long sampleInterval;

	public DefaultVibeBuilder()
	{
		sampleInterval = TimeUnit.MINUTES.toMillis(1);
	}
	
	@Override
	public VibeBuilder setBackend(VibeBackend backend)
	{
		this.backend = backend;
		return this;
	}
	
	@Override
	public VibeBuilder setBackends(VibeBackend... backends)
	{
		return setBackend(new MergedBackend(backends));
	}
	
	@Override
	public VibeBuilder setSampleInterval(long time, TimeUnit unit)
	{
		this.sampleInterval = unit.toMillis(time);
		
		return this;
	}
	
	@Override
	public Vibe build()
	{
		return new VibeImpl(backend, sampleInterval);
	}
}
