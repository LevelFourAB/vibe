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

	/**
	 * Subtract the given delta from the count.
	 *
	 * @param delta
	 */
	public void remove(long delta)
	{
		counter.addAndGet(-delta);
	}

	/**
	 * Set a new value.
	 *
	 * @param value
	 */
	public void set(long value)
	{
		counter.set(value);
	}

	@Override
	public Long read()
	{
		return counter.get();
	}
}
