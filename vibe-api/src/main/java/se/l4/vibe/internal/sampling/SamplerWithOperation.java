package se.l4.vibe.internal.sampling;

import se.l4.vibe.Handle;
import se.l4.vibe.operations.Operation;
import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.TimeSampler;

/**
 * {@link TimeSampler} that applies an {@link Operation} that can optionally
 * change the interval at which samples are created.
 *
 * @param <I>
 * @param <O>
 */
public class SamplerWithOperation<I, O>
	extends AbstractTimeSampler<O>
{
	private final TimeSampler<I> input;
	private final Operation<Sample<I>, Sample<O>> modifier;

	private Handle listenerHandle;

	public SamplerWithOperation(
		TimeSampler<I> input,
		Operation<Sample<I>, Sample<O>> modifier
	)
	{
		this.input = input;
		this.modifier = modifier;
	}

	@Override
	protected void startSampling()
	{
		listenerHandle = input.addListener(sample -> {
			Sample<O> nextSample = modifier.apply(sample);
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
		listenerHandle.release();
	}
}
