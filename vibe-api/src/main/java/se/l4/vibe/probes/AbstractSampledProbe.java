package se.l4.vibe.probes;

/**
 * Abstract implementation of {@link SampledProbe}. This class helps with the
 * implementation of {@link #read()} which always return the last value of
 * {@link #sample()}.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public abstract class AbstractSampledProbe<T>
	implements SampledProbe<T>
{
	protected T value;
	
	public AbstractSampledProbe()
	{
	}
	
	@Override
	public final T read()
	{
		return value;
	}

	@Override
	public T sample()
	{
		return (value = sample0());
	}
	
	/**
	 * Perform the real sampling.
	 * 
	 * @return
	 */
	protected abstract T sample0();

}
