package se.l4.vibe;

import java.util.concurrent.TimeUnit;

import se.l4.vibe.backend.VibeBackend;

/**
 * Builder for instances of {@link Vibe}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface VibeBuilder
{
	/**
	 * Set which backend to use.
	 * 
	 * @param backend
	 * @return
	 */
	VibeBuilder setBackend(VibeBackend backend);
	
	/**
	 * Set that several backends should be used.
	 * 
	 * @param backends
	 * @return
	 */
	VibeBuilder setBackends(VibeBackend... backends);

	/**
	 * Set at which interval samples should be taken by default.
	 * 
	 * @param time
	 * @param unit
	 * @return
	 */
	VibeBuilder setSampleInterval(long time, TimeUnit unit);

	/**
	 * Build the instance.
	 * 
	 * @return
	 */
	Vibe build();
}