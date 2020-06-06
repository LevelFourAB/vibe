package se.l4.vibe.internal.sampling;

import se.l4.vibe.sampling.Sample;

/**
 * Implementation of {@link Sample}.
 */
public class SampleImpl<T>
	implements Sample<T>
{
	private final long time;
	private final T value;

	public SampleImpl(long time, T value)
	{
		this.time = time;
		this.value = value;
	}

	@Override
	public long getTime()
	{
		return time;
	}

	@Override
	public T getValue()
	{
		return value;
	}

	@Override
	public String toString()
	{
		return "Sample{time=" + time + ", value=" + value + "}";
	}
}
