package se.l4.vibe.percentile;

import java.util.HashMap;
import java.util.Map;

import se.l4.vibe.mapping.KeyValueMappable;
import se.l4.vibe.mapping.KeyValueReceiver;
import se.l4.vibe.probes.AbstractSampledProbe;
import se.l4.vibe.probes.SampledProbe;

/**
 * Utility for combining several probes into a single one.
 *
 * @author Andreas Holstenson
 *
 */
public class CombinedProbes
{
	private CombinedProbes()
	{
	}

	public static <T> Builder<T> builder()
	{
		return new Builder<>();
	}

	/**
	 * Builder for probes.
	 *
	 * @author Andreas Holstenson
	 *
	 * @param <T>
	 */
	public static class Builder<T>
	{
		private final Map<String, SampledProbe<T>> probes;

		private Builder()
		{
			probes = new HashMap<>();
		}

		public Builder<T> add(String name, SampledProbe<T> probe)
		{
			probes.put(name, probe);
			return this;
		}

		public SampledProbe<CombinedData<T>> create()
		{
			return new CombinedProbe<>(probes);
		}
	}

	/**
	 * Probe that takes values from other probes and returns a {@link CombinedData}.
	 *
	 * @author Andreas Holstenson
	 *
	 * @param <T>
	 */
	private static class CombinedProbe<T>
		extends AbstractSampledProbe<CombinedData<T>>
	{
		private final Map<String, SampledProbe<T>> probes;

		public CombinedProbe(Map<String, SampledProbe<T>> probes)
		{
			this.probes = probes;
		}

		@Override
		public CombinedData<T> peek()
		{
			Map<String, T> values = new HashMap<>();
			for(Map.Entry<String, SampledProbe<T>> p : probes.entrySet())
			{
				values.put(p.getKey(), p.getValue().peek());
			}
			return new CombinedData<>(values);
		}

		@Override
		protected CombinedData<T> sample0()
		{
			Map<String, T> values = new HashMap<>();
			for(Map.Entry<String, SampledProbe<T>> p : probes.entrySet())
			{
				values.put(p.getKey(), p.getValue().sample());
			}
			return new CombinedData<>(values);
		}
	}

	/**
	 * Combination of data from several probes.
	 *
	 * @author Andreas Holstenson
	 *
	 * @param <T>
	 */
	public static class CombinedData<T>
		implements KeyValueMappable
	{
		private final Map<String, T> values;

		public CombinedData(Map<String, T> values)
		{
			this.values = values;
		}

		public T get(String id)
		{
			return values.get(id);
		}

		@Override
		public void mapToKeyValues(KeyValueReceiver receiver)
		{
			for(Map.Entry<String, T> e : values.entrySet())
			{
				receiver.add(e.getKey(), e.getValue());
			}
		}
	}
}
