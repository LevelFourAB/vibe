package se.l4.vibe.trigger;

import java.util.concurrent.TimeUnit;

import se.l4.vibe.probes.Average;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.Range;
import se.l4.vibe.probes.TimeSeries;

/**
 * Triggers that can be used together with monitoring.
 * 
 * @author Andreas Holstenson
 *
 */
public class Triggers
{
	private Triggers()
	{
	}
	
	/**
	 * Get a trigger that will return the exact value of a probe.
	 * 
	 * @return
	 */
	public static <T> Trigger<T, T> valueIs()
	{
		return new Trigger<T, T>()
		{
			@Override
			public Probe<T> forTimeSeries(TimeSeries<T> series)
			{
				return series.getProbe();
			}
			
			@Override
			public String toString()
			{
				return "value";
			}
		};
	}
	
	/**
	 * Calculate the average for a specific duration and check conditions
	 * against the specified value.
	 * 
	 * @param duration
	 * @param unit
	 * @return
	 */
	public static <T extends Number> Trigger<T, Double> average(final long duration, final TimeUnit unit)
	{
		return new Trigger<T, Double>()
		{
			@Override
			public Probe<Double> forTimeSeries(TimeSeries<T> series)
			{
				return Average.forSeries(series, duration, unit);
			}
			
			@Override
			public String toString()
			{
				return "average over " + duration + " " + toReadable(unit, duration);
			}
		};
	}
	
	/**
	 * Calculate the minimum value over a specific duration.
	 * 
	 * @param duration
	 * @param unit
	 * @return
	 */
	public static <T extends Number> Trigger<T, Double> minimum(final long duration, final TimeUnit unit)
	{
		return new Trigger<T, Double>()
		{
			@Override
			public Probe<Double> forTimeSeries(TimeSeries<T> series)
			{
				return Range.min(series, duration, unit);
			}
			
			@Override
			public String toString()
			{
				return "minimum over " + duration + " " + toReadable(unit, duration);
			}
		};
	}
	
	/**
	 * Calculate the maximum value over a specific duration.
	 * 
	 * @param duration
	 * @param unit
	 * @return
	 */
	public static <T extends Number> Trigger<T, Double> maximum(final long duration, final TimeUnit unit)
	{
		return new Trigger<T, Double>()
		{
			@Override
			public Probe<Double> forTimeSeries(TimeSeries<T> series)
			{
				return Range.max(series, duration, unit);
			}
			
			@Override
			public String toString()
			{
				return "maximum over " + duration + " " + toReadable(unit, duration);
			}
		};
	}
	
	private static String toReadable(TimeUnit unit, long duration)
	{
		boolean one = duration == 1;
		
		switch(unit)
		{
			case DAYS:
				return one ? "day" : "days";
			case HOURS:
				return one ? "hour" : "hours";
			case MICROSECONDS:
				return one ? "microsecond" : "microseconds";
			case MILLISECONDS:
				return one ? "millisecond" : "milliseconds";
			case MINUTES:
				return one ? "minute" : "minutes";
			case NANOSECONDS:
				return one ? "nanosecond" : "nanoseconds";
			case SECONDS:
				return one ? "second" : "seconds";
		}
		
		return unit.toString();
	}
}
