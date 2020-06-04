package se.l4.vibe.internal.jmx;

import se.l4.vibe.internal.service.ExposeAsAttribute;
import se.l4.vibe.probes.Sampler;

public class SamplerBean
{
	private final Sampler<?> series;

	public SamplerBean(Sampler<?> series)
	{
		this.series = series;
	}

	@ExposeAsAttribute
	public Object currentValue()
	{
		return series.getProbe().peek();
	}
}
