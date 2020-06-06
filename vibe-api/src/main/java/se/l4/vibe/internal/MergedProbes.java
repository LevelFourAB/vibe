package se.l4.vibe.internal;

import java.util.ArrayList;
import java.util.List;

import se.l4.vibe.mapping.KeyValueMap;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.SampledProbe;

public class MergedProbes
{
	public static class ProbeBuilder
		implements Probe.MergedBuilder
	{
		private final List<Named<Probe<?>>> probes;

		public ProbeBuilder()
		{
			probes = new ArrayList<>();
		}

		@Override
		public Probe.MergedBuilder add(String name, Probe<?> probe)
		{
			probes.add(new Named<>(name, probe));
			return this;
		}

		@Override
		public Probe<KeyValueMap> build()
		{
			return new MergedProbe(probes);
		}
	}

	public static class SampledProbeBuilder
		implements SampledProbe.MergedBuilder
	{
		private final List<Named<SampledProbe<?>>> probes;

		public SampledProbeBuilder()
		{
			probes = new ArrayList<>();
		}

		@Override
		public SampledProbe.MergedBuilder add(String name, SampledProbe<?> probe)
		{
			probes.add(new Named<>(name, probe));
			return this;
		}

		@Override
		public SampledProbe<KeyValueMap> build()
		{
			return new MergedSampledProbe(probes);
		}
	}

	public static class Named<T>
	{
		private final String name;
		private final T value;

		public Named(String key, T value)
		{
			this.name = key;
			this.value = value;
		}
	}

	public static class MergedProbe
		implements Probe<KeyValueMap>
	{
		private final Named<Probe<?>>[] values;

		@SuppressWarnings({ "unchecked" })
		public MergedProbe(List<Named<Probe<?>>> values)
		{
			this.values = values.toArray(new Named[values.size()]);
		}

		@Override
		public KeyValueMap read()
		{
			KeyValueMap result = new KeyValueMap();
			for(Named<Probe<?>> value : values)
			{
				result.set(value.name, value.value.read());
			}

			return result;
		}
	}

	public static class MergedSampledProbe
		implements SampledProbe<KeyValueMap>
	{
		private final Named<SampledProbe<?>>[] values;

		@SuppressWarnings({ "unchecked" })
		public MergedSampledProbe(List<Named<SampledProbe<?>>> values)
		{
			this.values = values.toArray(new Named[values.size()]);
		}

		@Override
		public KeyValueMap sample()
		{
			KeyValueMap result = new KeyValueMap();
			for(Named<SampledProbe<?>> value : values)
			{
				result.set(value.name, value.value.sample());
			}

			return result;
		}
	}
}
