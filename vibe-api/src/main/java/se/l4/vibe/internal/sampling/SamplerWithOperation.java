package se.l4.vibe.internal.sampling;

import se.l4.vibe.ListenerHandle;
import se.l4.vibe.sampling.AbstractSampler;
import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.SampleOperation;
import se.l4.vibe.sampling.Sampler;

/**
 * {@link Sampler} that applies a {@link SampleOperation}.
 *
 * @param <I>
 * @param <O>
 */
public class SamplerWithOperation<I, O>
	extends AbstractSampler<O>
{
	private final Sampler<I> input;
	private final SampleOperation<I, O> modifier;

	private ListenerHandle listenerHandle;

	public SamplerWithOperation(
		Sampler<I> input,
		SampleOperation<I, O> modifier
	)
	{
		this.input = input;
		this.modifier = modifier;
	}

	@Override
	protected void startSampling()
	{
		listenerHandle = input.addListener(sample -> {
			Sample<O> nextSample = modifier.handleSample(sample);
			Sample<O> lastSample = lastSample();

			if(lastSample == null || nextSample.getTime() != lastSample.getTime())
			{
				/*
				 * If there is no sample or if the next sample is for a new
				 * time register it.
				 */
				registerSample(nextSample.getTime(), nextSample.getValue());
			}
		});
	}

	@Override
	protected void stopSampling()
	{
		listenerHandle.remove();
	}
}
