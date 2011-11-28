package se.l4.vibe.internal;

/**
 * Key to hold a sample interval plus a sample retention time.
 * 
 * @author Andreas Holstenson
 *
 */
public class SampleTime
{
	private final long interval;
	private final long retention;

	public SampleTime(long interval, long retention)
	{
		this.interval = interval;
		this.retention = retention;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (interval ^ (interval >>> 32));
		result = prime * result + (int) (retention ^ (retention >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		SampleTime other = (SampleTime) obj;
		if(interval != other.interval)
			return false;
		if(retention != other.retention)
			return false;
		return true;
	}
}
