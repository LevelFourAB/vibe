package se.l4.vibe.internal;

import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.TimeSeries;
import se.l4.vibe.trigger.TimedTrigger;
import se.l4.vibe.trigger.Trigger;

/**
 * Implementation of {@link Trigger} that combines two other triggers.
 * 
 * @author Andreas Holstenson
 *
 * @param <Input>
 * @param <Output>
 */
public class MergedTrigger<Input, Output>
	implements TimedTrigger<Input, Output>
{
	private final Trigger<Input, ?> first;
	private final Trigger<?, Output> second;

	public <T> MergedTrigger(
			Trigger<Input, T> first,
			Trigger<T, Output> second)
	{
		this.first = first;
		this.second = second;
	}
	
	@Override
	public Probe<Output> forTimeSeries(TimeSeries<Input> series)
	{
		Probe<?> probe = first.forTimeSeries(series);
		TimeSeriesForMergedTrigger fakeSeries = new TimeSeriesForMergedTrigger(series, probe);
		return second.forTimeSeries(fakeSeries);
	}
	
	@Override
	public long getDefaultRepeatTime()
	{
		if(second instanceof TimedTrigger)
		{
			// Get the time of the second trigger if set
			long time = ((TimedTrigger) second).getDefaultRepeatTime();
			if(time > 0) return time;
		}
		
		if(first instanceof TimedTrigger)
		{
			// Get the time of the first trigger if set
			long time = ((TimedTrigger) first).getDefaultRepeatTime();
			if(time > 0) return time;
		}
		
		// Set to sample time by default
		return 0;
	}
	
	@Override
	public String toString()
	{
		return second + " on " + first;
	}
}