package se.l4.vibe.probes;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Probe used for counting values.
 */
public class CountingProbe
	implements Probe<Long>
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
	 * Decrease the count with one.
	 *
	 */
	public void decrease()
	{
		counter.decrementAndGet();
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
	public Long read()
	{
		return counter.get();
	}
}
