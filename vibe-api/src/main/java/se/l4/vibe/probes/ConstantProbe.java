package se.l4.vibe.probes;

/**
 * Probe that holds a constant value.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class ConstantProbe<T>
	extends AbstractSampledProbe<T>
{
	private final T value;
	
	public ConstantProbe(T value)
	{
		this.value = value;
	}
	
	/**
	 * Create a new probe for the given object.
	 * 
	 * @param object
	 * @return
	 */
	public static <T> ConstantProbe<T> forValue(T object)
	{
		return new ConstantProbe<T>(object);
	}
	
	@Override
	protected T sample0()
	{
		return value;
	}
	
	@Override
	public T peek()
	{
		return value;
	}
}
