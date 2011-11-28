package se.l4.vibe.internal.jmx;

import se.l4.vibe.probes.TimeSeries;
import se.l4.vibe.service.ExposeAsAttribute;

public class TimeSeriesBean
{
	private final TimeSeries<?> series;

	public TimeSeriesBean(TimeSeries<?> series)
	{
		this.series = series;
	}
	
	@ExposeAsAttribute
	public Object currentValue()
	{
		return series.getProbe().peek();
	}
}
