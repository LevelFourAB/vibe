package se.l4.vibe.trigger;

import java.util.concurrent.TimeUnit;

import se.l4.vibe.internal.MergedTrigger;
import se.l4.vibe.probes.Average;
import se.l4.vibe.probes.Change;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.Range;
import se.l4.vibe.probes.Sum;
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
	public static <T> Trigger<T, T> value()
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
	 * Get a trigger that will return the exact value of a probe as a number.
	 * 
	 * @return
	 */
	public static <T extends Number> Trigger<T, Number> numericValue()
	{
		return new Trigger<T, Number>()
		{
			@Override
			public Probe<Number> forTimeSeries(TimeSeries<T> series)
			{
				return (Probe) series.getProbe();
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
	public static <T extends Number> Trigger<T, Number> averageOver(final long duration, final TimeUnit unit)
	{
		return new TimedTrigger<T, Number>()
		{
			@Override
			public Probe<Number> forTimeSeries(TimeSeries<T> series)
			{
				return (Probe) Average.forSeries(series, duration, unit);
			}
			
			@Override
			public long getDefaultRepeatTime()
			{
				return unit.toMillis(duration);
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
	public static <T extends Number> Trigger<T, Number> minimumOver(final long duration, final TimeUnit unit)
	{
		return new TimedTrigger<T, Number>()
		{
			@Override
			public Probe<Number> forTimeSeries(TimeSeries<T> series)
			{
				return (Probe) Range.minimum(series, duration, unit);
			}
			
			@Override
			public long getDefaultRepeatTime()
			{
				return unit.toMillis(duration);
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
	public static <T extends Number> Trigger<T, Number> maximumOver(final long duration, final TimeUnit unit)
	{
		return new TimedTrigger<T, Number>()
		{
			@Override
			public Probe<Number> forTimeSeries(TimeSeries<T> series)
			{
				return (Probe) Range.maximum(series, duration, unit);
			}
			
			@Override
			public long getDefaultRepeatTime()
			{
				return unit.toMillis(duration);
			}
			
			@Override
			public String toString()
			{
				return "maximum over " + duration + " " + toReadable(unit, duration);
			}
		};
	}
	
	/**
	 * Calculate the change between sample values.
	 * 
	 * @param duration
	 * @param unit
	 * @return
	 */
	public static <T extends Number> Trigger<T, Number> change()
	{
		return new Trigger<T, Number>()
		{
			@Override
			public Probe<Number> forTimeSeries(TimeSeries<T> series)
			{
				return Change.forSeries(series);
			}
			
			@Override
			public String toString()
			{
				return "change";
			}
		};
	}
	
	/**
	 * Calculate the change between sample values and return it as a fraction.
	 * 
	 * @param duration
	 * @param unit
	 * @return
	 */
	public static <T extends Number> Trigger<T, Number> changeAsFraction()
	{
		return new Trigger<T, Number>()
		{
			@Override
			public Probe<Number> forTimeSeries(TimeSeries<T> series)
			{
				return (Probe) Change.asFraction(series);
			}
			
			@Override
			public String toString()
			{
				return "change as fraction";
			}
		};
	}
	
	/**
	 * Calculate the sum.
	 * 
	 * @param duration
	 * @param unit
	 * @return
	 */
	public static <T extends Number> Trigger<T, Number> sum()
	{
		return new Trigger<T, Number>()
		{
			@Override
			public Probe<Number> forTimeSeries(TimeSeries<T> series)
			{
				return (Probe) Sum.forSeriesAsDouble(series);
			}
			
			@Override
			public String toString()
			{
				return "sum";
			}
		};
	}
	
	/**
	 * Calculate the sum for a specific duration and check conditions
	 * against the specified value.
	 * 
	 * @param duration
	 * @param unit
	 * @return
	 */
	public static <T extends Number> Trigger<T, Number> sumOver(final long duration, final TimeUnit unit)
	{
		return new TimedTrigger<T, Number>()
		{
			@Override
			public Probe<Number> forTimeSeries(TimeSeries<T> series)
			{
				return (Probe) Sum.forSeriesAsDouble(series, duration, unit);
			}
			
			@Override
			public long getDefaultRepeatTime()
			{
				return unit.toMillis(duration);
			}
			
			@Override
			public String toString()
			{
				return "sum over " + duration + " " + toReadable(unit, duration);
			}
		};
	}

	/**
	 * Create a middle value to perform triggering on. This allows one to
	 * to create triggers on values returned by other triggers.
	 * 
	 * <p>
	 * Example:
	 * <pre>
	 * when(change(), on(sum()), above(0.8))
	 * </pre>
	 * 
	 * @param trigger
	 * @return
	 */
	public static <Input, Output> On<Input, Output> on(
		final Trigger<Input, Output> trigger
	)
	{
		return new On<Input, Output>()
		{
			@Override
			public <T> Trigger<Input, T> build(Trigger<Output, T> second)
			{
				return new MergedTrigger<Input, T>(trigger, second);
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
