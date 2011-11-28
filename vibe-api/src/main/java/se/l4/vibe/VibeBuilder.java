package se.l4.vibe;

import java.util.concurrent.TimeUnit;

import se.l4.vibe.backend.LoggingBackend;
import se.l4.vibe.backend.VibeBackend;
import se.l4.vibe.internal.DefaultVibe;

/**
 * Builder for {@link Vibe} instances.
 * 
 * @author Andreas Holstenson
 *
 */
public class VibeBuilder
{
	private VibeBackend backend;
	private long sampleInterval;
	private long sampleRetention;

	public VibeBuilder()
	{
		backend = new LoggingBackend();
		sampleInterval = TimeUnit.MINUTES.toMillis(1);
		sampleRetention = TimeUnit.MINUTES.toMillis(60);
	}
	
	/**
	 * Set which backend to use.
	 * 
	 * @param backend
	 * @return
	 */
	public VibeBuilder setBackend(VibeBackend backend)
	{
		this.backend = backend;
		return this;
	}
	
	/**
	 * Set at which interval samples should be taken by default.
	 * 
	 * @param time
	 * @param unit
	 * @return
	 */
	public VibeBuilder setSamplingInterval(long time, TimeUnit unit)
	{
		this.sampleInterval = unit.toMillis(time);
		
		return this;
	}
	
	/**
	 * Set how long samples should be retained for.
	 * 
	 * @param time
	 * @param unit
	 * @return
	 */
	public VibeBuilder setSampleRetention(long time, TimeUnit unit)
	{
		this.sampleRetention = unit.toMillis(time);
		
		return this;
	}
	
	public VibeBuilder setLogging(boolean logging)
	{
		return this;
	}
	
	public Vibe build()
	{
		return new DefaultVibe(backend, sampleInterval, sampleRetention);
	}
}
