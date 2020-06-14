package se.l4.vibe.internal.sampling;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.Handle;
import se.l4.vibe.internal.Scheduling;
import se.l4.vibe.operations.Operation;
import se.l4.vibe.operations.OperationExecutor;
import se.l4.vibe.operations.TimeSampleOperation;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.Sampler;
import se.l4.vibe.sampling.TimeSampler;

public class TimeSamplerImpl<T>
	extends AbstractTimeSampler<T>
{
	private final long intervalTime;
	private final SampledProbe<T> probe;

	private Handle handle;
	private Sampler<T> sampler;

	public TimeSamplerImpl(
		SampledProbe<T> probe,
		long intervalTime
	)
	{
		this.intervalTime = intervalTime;
		this.probe = probe;
	}

	/**
	 * Sample the probe and register the sample. Synchronized so that the
	 * sampling and listeners are run in a thread-safe way.
	 */
	private void sample()
	{
		synchronized(this)
		{
			long time = System.currentTimeMillis();
			T value = sampler.sample();

			registerSample(time, value);
		}
	}

	@Override
	protected void startSampling()
	{
		// Create the sampler to use
		sampler = probe.create();

		// Perform the initial sampling
		sample();

		// The schedule sampling
		handle = Scheduling.scheduleSampling(intervalTime, this::sample);
	}

	@Override
	protected void stopSampling()
	{
		handle.release();
		sampler.release();
	}

	public static class BuilderImpl<T>
		implements Builder<T>
	{
		private SampledProbe<?> probe;
		private long interval;

		private List<Operation<Sample<?>, Sample<?>>> ops;

		public BuilderImpl(SampledProbe<?> probe)
		{
			this.probe = probe;
			interval = TimeUnit.SECONDS.toMillis(10);
		}

		@Override
		public Builder<T> withInterval(long time, TimeUnit unit)
		{
			this.interval = unit.toMillis(time);
			return this;
		}

		@Override
		public Builder<T> withInterval(Duration time)
		{
			this.interval = time.toMillis();
			return this;
		}

		@Override
		public <O> Builder<O> apply(Operation<T, O> operation)
		{
			return applyResampling(TimeSampleOperation.over(operation));
		}

		@Override
		public <O> Builder<O> apply(OperationExecutor<T, O> executor)
		{
			return apply(() -> executor);
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <O> Builder<O> applyResampling(Operation<Sample<T>, Sample<O>> operation)
		{
			if(ops == null)
			{
				ops = new ArrayList<>();
			}

			ops.add((Operation) operation);

			return (Builder) this;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public TimeSampler<T> build()
		{
			TimeSampler sampler = new TimeSamplerImpl<>(probe, interval);

			if(ops != null)
			{
				// Apply the operations if they exist
				for(Operation<Sample<?>, Sample<?>> op : ops)
				{
					sampler = sampler.applyResampling(op);
				}
			}

			return sampler;
		}
	}
}
