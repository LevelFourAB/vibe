package se.l4.vibe.internal.jmx;

import se.l4.vibe.internal.service.ExposeAsAttribute;
import se.l4.vibe.probes.Sampler;

public class TimeSeriesBean
{
	private final Sampler<?> series;

	public TimeSeriesBean(Sampler<?> series)
	{
		this.series = series;
	}
	
	@ExposeAsAttribute
	public Object currentValue()
	{
		return series.getProbe().peek();
	}
}
