package se.l4.vibe;

import java.util.concurrent.TimeUnit;

import se.l4.vibe.backend.LoggingBackend;
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
	private long sampleRetention;

	public DefaultVibeBuilder()
	{
		backend = new LoggingBackend();
		sampleInterval = TimeUnit.MINUTES.toMillis(1);
		sampleRetention = TimeUnit.MINUTES.toMillis(60);
	}
	
	@Override
	public VibeBuilder setBackend(VibeBackend backend)
	{
		this.backend = backend;
		return this;
	}
	
	@Override
	public VibeBuilder setSampleInterval(long time, TimeUnit unit)
	{
		this.sampleInterval = unit.toMillis(time);
		
		return this;
	}
	
	@Override
	public VibeBuilder setSampleRetention(long time, TimeUnit unit)
	{
		this.sampleRetention = unit.toMillis(time);
		
		return this;
	}
	
	public VibeBuilder setLogging(boolean logging)
	{
		return this;
	}
	
	@Override
	public Vibe build()
	{
		return new DefaultVibe(backend, sampleInterval, sampleRetention);
	}
}
