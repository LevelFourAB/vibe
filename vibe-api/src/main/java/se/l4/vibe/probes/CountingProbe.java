package se.l4.vibe.probes;

import java.util.concurrent.atomic.LongAdder;

/**
 * Probe used for counting values.
 */
public class CountingProbe
	implements Probe<Long>
{
	private final LongAdder counter;

	public CountingProbe()
	{
		counter = new LongAdder();
	}

	/**
	 * Increase the count with one.
	 *
	 */
	public void increase()
	{
		counter.increment();
	}

	/**
	 * Decrease the count with one.
	 *
	 */
	public void decrease()
	{
		counter.decrement();
	}

	/**
	 * Add the given delta to the count.
	 *
	 * @param count
	 */
	public void add(long delta)
	{
		counter.add(delta);
	}

	/**
	 * Subtract the given delta from the count.
	 *
	 * @param delta
	 */
	public void remove(long delta)
	{
		counter.add(-delta);
	}

	@Override
	public Long read()
	{
		return counter.sum();
	}
}
