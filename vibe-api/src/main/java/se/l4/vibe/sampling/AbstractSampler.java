package se.l4.vibe.sampling;

import se.l4.vibe.ListenerHandle;
import se.l4.vibe.Listeners;
import se.l4.vibe.internal.sampling.SampleImpl;

/**
 * Abstract implementation of {@link Sampler}.
 *
 * @param <T>
 */
public abstract class AbstractSampler<T>
	implements Sampler<T>
{
	private final Listeners<SampleListener<T>> listeners;

	private Sample<T> lastSample;

	public AbstractSampler()
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
	public ListenerHandle addListener(SampleListener<T> listener)
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
