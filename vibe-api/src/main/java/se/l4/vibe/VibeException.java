package se.l4.vibe;

/**
 * Exception for monitoring errors.
 * 
 * @author Andreas Holstenson
 *
 */
public class VibeException
	extends RuntimeException
{

	public VibeException()
	{
		super();
	}

	public VibeException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public VibeException(String message)
	{
		super(message);
	}

	public VibeException(Throwable cause)
	{
		super(cause);
	}
	
}
