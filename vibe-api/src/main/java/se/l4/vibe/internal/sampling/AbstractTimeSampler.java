package se.l4.vibe.internal.sampling;

import se.l4.vibe.Handle;
import se.l4.vibe.internal.Listeners;
import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.SampleListener;
import se.l4.vibe.sampling.TimeSampler;

/**
 * Abstract implementation of {@link TimeSampler}.
 *
 * @param <T>
 */
public abstract class AbstractTimeSampler<T>
	implements TimeSampler<T>
{
	private final Listeners<SampleListener<T>> listeners;

	private Sample<T> lastSample;

	public AbstractTimeSampler()
	{
		listeners = new Listeners<>(count -> {
			if(count == 1)
			{
				startSampling();
			}
			else if(count == 0)
			{
				stopSampling();
			}
		});
	}

	@Override
	public T read()
	{
		return lastSample == null ? null : lastSample.getValue();
	}

	@Override
	public Sample<T> getLastSample()
	{
		return lastSample;
	}

	@Override
	public Handle start()
	{
		return addListener(sample -> {});
	}

	@Override
	public Handle addListener(SampleListener<T> listener)
	{
		return listeners.add(listener);
	}

	@Override
	public void removeListener(SampleListener<T> listener)
	{
		listeners.remove(listener);
	}

	protected void registerSample(long time, T value)
	{
		// TODO: Concurrency

		Sample<T> sample = new SampleImpl<>(time, value);
		lastSample = sample;
		listeners.forEach(l -> l.sampleAcquired(sample));
	}

	protected Sample<T> lastSample()
	{
		return lastSample;
	}

	protected void startSampling()
	{
	}

	protected void stopSampling()
	{
	}
}
