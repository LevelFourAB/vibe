package se.l4.vibe.internal;

import se.l4.vibe.probes.AbstractSampler;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampleListener;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.probes.Sampler;
import se.l4.vibe.trigger.On;

/**
 * Implementation of {@link Sampler} for use {@link On}.
 *
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class SamplerForMergedTrigger<T>
	extends AbstractSampler<T>
{
	public <In> SamplerForMergedTrigger(Sampler<In> input, final Probe<T> probe)
	{
		input.addListener(new SampleListener<In>()
		{
			@Override
			public void sampleAcquired(SampledProbe<In> probe0, Entry<In> entry)
			{
				SampleListener<T>[] listeners0 = listeners;
				Entry<T> newEntry = createEntry(entry.getTime(), probe.read());
				for(SampleListener<T> l : listeners0)
				{
					l.sampleAcquired(null, newEntry);
				}
			}
		});
	}

	@Override
	public SampledProbe<T> getProbe()
	{
		return null;
	}
}
