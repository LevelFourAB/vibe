package se.l4.vibe.probes;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A probe that counts upwards.
 * 
 * @author Andreas Holstenson
 *
 */
public class CountingProbe
	extends AbstractSampledProbe<Long>
{
	private final AtomicLong counter;

	public CountingProbe()
	{
		counter = new AtomicLong();
	}

	/**
	 * Increase the count with one.
	 * 
	 */
	public void increase()
	{
		counter.incrementAndGet();
	}
	
	/**
	 * Add the given delta to the count.
	 * 
	 * @param count
	 */
	public void add(long delta)
	{
		counter.addAndGet(delta);
	}

	@Override
	public Long peek()
	{
		return counter.get();
	}

	@Override
	protected Long sample0()
	{
		return counter.getAndSet(0);
	}

}