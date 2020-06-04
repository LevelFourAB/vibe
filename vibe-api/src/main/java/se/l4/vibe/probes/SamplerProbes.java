package se.l4.vibe.probes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.probes.Sampler.Entry;

/**
 * General probes that work with {@link Sampler sampler}.
 *
 * @author Andreas Holstenson
 *
 */
public class SamplerProbes
{

	private SamplerProbes()
	{
	}

	/**
	 * Create a probe for the specified sampler that will keep track of values
	 * over the specified time.
	 *
	 * @param series
	 * @param duration
	 * @param unit
	 * @param operation
	 * @return
	 */
	public static <Input, Output> Probe<Output> forSampler(
		Sampler<Input> series,
		long duration, TimeUnit unit,
		SampleOperation<Input, Output> operation
	)
	{
		return new TimeLimitedProbe<Input, Output>(duration, unit, series, operation);
	}

	/**
	 * Create a probe for the specified series that will keep track of values
	 * over the specified time.
	 *
	 * @param series
	 * @param duration
	 * @param unit
	 * @return
	 */
	public static <T extends ModifiableData<T>> Probe<T> forSampler(
		Sampler<T> series,
		long duration, TimeUnit unit
	)
	{
		return forSampler(series, duration, unit, new ModifiableDataOperation<T>());
	}

	/**
	 * Create a probe for the specified series that keep track of values
	 * ever sampled.
	 *
	 * <p>
	 * The given operation will <b>not</b> have access to the entire range of
	 * {@link Sampler.Entry entries} in the
	 * {@link SampleOperation#add(Object, java.util.Collection) add method}.
	 *
	 * @param series
	 * @param operation
	 * @return
	 */
	public static <Input, Output> Probe<Output> forSampler(
		Sampler<Input> series,
		SampleOperation<Input, Output> operation
	)
	{
		return new EternityProbe<Input, Output>(series, operation);
	}

	/**
	 * Create a probe for the specified series that keep track of values
	 * ever sampled.
	 *
	 * <p>
	 * This is can be used if the data stored in the series implements
	 * {@link ModifiableData}.
	 *
	 * @param series
	 * @return
	 */
	public static <T extends ModifiableData<T>> Probe<T> forSampler(Sampler<T> series)
	{
		return forSampler(series, new ModifiableDataOperation<T>());
	}

	private static class EternityProbe<Input, Output>
		implements Probe<Output>
	{
		private final SampleOperation<Input, Output> operation;

		public EternityProbe(Sampler<Input> series, SampleOperation<Input, Output> operation)
		{
			this.operation = operation;

			series.addListener(new SampleListener<Input>()
			{
				@Override
				public void sampleAcquired(SampledProbe<Input> probe, Sampler.Entry<Input> value)
				{
					handleSampleAcquired(value);
				}
			});
		}

		protected void handleSampleAcquired(Entry<Input> value)
		{
			operation.add(value.getValue(), null);
		}

		@Override
		public Output read()
		{
			return operation.get();
		}
	}

	/**
	 * Probe that is time limited based on the input of a
	 * {@link Sampler}.
	 *
	 * @author Andreas Holstenson
	 *
	 */
	private static class TimeLimitedProbe<Input, Output>
		implements Probe<Output>
	{
		private final SampleOperation<Input, Output> operation;
		private final long maxAge;
		private final List<Sampler.Entry<Input>> entries;

		/**
		 * Create a new time limited probe that will be limited to the specified
		 * time. The operation is used to calculate the value of the probe.
		 *
		 * @param time
		 * @param unit
		 * @param operation
		 */
		public TimeLimitedProbe(
				long time, TimeUnit unit,
				Sampler<Input> series,
				SampleOperation<Input, Output> operation)
		{
			maxAge = unit.toMillis(time);
			this.operation = operation;
			entries = new LinkedList<Sampler.Entry<Input>>();

			series.addListener(new SampleListener<Input>()
			{
				@Override
				public void sampleAcquired(SampledProbe<Input> probe, Sampler.Entry<Input> value)
				{
					handleSampleAcquired(value);
				}
			});
		}

		private void handleSampleAcquired(Sampler.Entry<Input> entry)
		{
			if(! entries.isEmpty())
			{
				/*
				 * If we have entries check if the first one should be
				 * removed or kept.
				 */
				Sampler.Entry<Input> firstEntry = entries.get(0);
				if(firstEntry.getTime() < System.currentTimeMillis() - maxAge)
				{
					entries.remove(0);

					operation.remove(firstEntry.getValue(), entries);
				}
			}

			entries.add(entry);
			operation.add(entry.getValue(), entries);
		}

		@Override
		public Output read()
		{
			return operation.get();
		}
	}

	private static class ModifiableDataOperation<T extends ModifiableData<T>>
		implements SampleOperation<T, T>
	{
		private T data;

		@Override
		public void add(T value, Collection<Entry<T>> entries)
		{
			if(data == null)
			{
				data = value;
				return;
			}

			data = data.add(value);
		}

		@Override
		public void remove(T value, Collection<Entry<T>> entries)
		{
			if(data == null) return;

			data = data.remove(value);
		}

		@Override
		public T get()
		{
			return data;
		}
	}
}
