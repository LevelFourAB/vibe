package se.l4.vibe.internal.sampling;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.Handle;
import se.l4.vibe.internal.Scheduling;
import se.l4.vibe.sampling.AbstractSampler;
import se.l4.vibe.sampling.SampleOperation;
import se.l4.vibe.sampling.SampledProbe;
import se.l4.vibe.sampling.Sampler;

public class SamplerImpl<T>
	extends AbstractSampler<T>
{
	private final long intervalTime;
	private final SampledProbe<T> probe;

	private Handle handle;

	public SamplerImpl(
		SampledProbe<T> probe,
		long intervalTime
	)
	{
		this.intervalTime = intervalTime;
		this.probe = probe;
	}

	private void sample()
	{
		long time = System.currentTimeMillis();
		T value = probe.sample();

		registerSample(time, value);
	}

	@Override
	protected void startSampling()
	{
		// Perform the initial sampling
		sample();

		// The schedule sampling
		handle = Scheduling.scheduleSampling(intervalTime, this::sample);
	}

	@Override
	protected void stopSampling()
	{
		handle.release();
	}

	public static class BuilderImpl<T>
		implements Builder<T>
	{
		private SampledProbe<?> probe;
		private long interval;

		@SuppressWarnings({ "rawtypes" })
		private List<SampleOperation> ops;

		public BuilderImpl(SampledProbe<?> probe)
		{
			this.probe = probe;
			interval = TimeUnit.SECONDS.toMillis(10);
		}

		@Override
		public Builder<T> setInterval(long time, TimeUnit unit)
		{
			this.interval = unit.toMillis(time);
			return this;
		}

		@Override
		public Builder<T> setInterval(Duration time)
		{
			this.interval = time.toMillis();
			return this;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <O> Builder<O> apply(SampleOperation<T, O> operation)
		{
			if(ops == null)
			{
				ops = new ArrayList<>();
			}

			ops.add(operation);

			return (Builder) this;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Sampler<T> build()
		{
			Sampler sampler = new SamplerImpl<>(probe, interval);

			if(ops != null)
			{
				// Apply the operations if they exist
				for(SampleOperation op : ops)
				{
					sampler = sampler.apply(op);
				}
			}

			return sampler;
		}
	}
}
