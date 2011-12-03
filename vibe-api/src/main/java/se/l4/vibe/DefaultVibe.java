package se.l4.vibe;

import se.l4.vibe.internal.DefaultVibeBuilder;

/**
 * Entry point to start building a default implementation of {@link Vibe}.
 * 
 * @author Andreas Holstenson
 *
 */
public class DefaultVibe
{
	/**
	 * Start building a new {@link Vibe}.
	 * 
	 * @return
	 */
	public static VibeBuilder builder()
	{
		return new DefaultVibeBuilder();
	}
	
	private DefaultVibe()
	{
	}
}
