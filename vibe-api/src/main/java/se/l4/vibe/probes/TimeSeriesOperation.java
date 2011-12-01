package se.l4.vibe.probes;

import java.util.Collection;

/**
 * Operation that performs all calculations for the probe.
 * 
 * @author Andreas Holstenson
 *
 * @param <Input>
 * @param <Output>
 */
public interface TimeSeriesOperation<Input, Output>
{
	/**
	 * A value has been removed.
	 * 
	 * @param value
	 * 		the actual value that was removed
	 * @param entries
	 * 		collection with all of the entries
	 */
	void remove(Input value, Collection<TimeSeries.Entry<Input>> entries);

	/**
	 * A value has been added.
	 * 
	 * @param value
	 * 		the actual value that was added
	 * @param entries
	 * 		collection with all of the entries
	 */
	void add(Input value, Collection<TimeSeries.Entry<Input>> entries);

	/**
	 * Get the current calculated value.
	 * 
	 * @return
	 */
	Output get();
}
