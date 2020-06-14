package se.l4.vibe.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.l4.vibe.internal.MergedProbes.Named;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.sampling.Sampler;
import se.l4.vibe.snapshots.MapSnapshot;

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
		public Probe<MapSnapshot> build()
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
		public SampledProbe<MapSnapshot> build()
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
		implements Probe<MapSnapshot>
	{
		private final Named<Probe<?>>[] values;

		@SuppressWarnings({ "unchecked" })
		public MergedProbe(List<Named<Probe<?>>> values)
		{
			this.values = values.toArray(new Named[values.size()]);
		}

		@Override
		public MapSnapshot read()
		{
			MapSnapshot.Builder builder = MapSnapshot.builder();
			for(Named<Probe<?>> value : values)
			{
				builder.set(value.name, value.value.read());
			}

			return builder.build();
		}
	}

	public static class MergedSampledProbe
		implements SampledProbe<MapSnapshot>
	{
		private final Named<SampledProbe<?>>[] values;

		@SuppressWarnings({ "unchecked" })
		public MergedSampledProbe(List<Named<SampledProbe<?>>> values)
		{
			this.values = values.toArray(new Named[values.size()]);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Sampler<MapSnapshot> create()
		{
			Named<Sampler<?>>[] samplers = Arrays.stream(values)
				.map(n -> new Named<>(n.name, n.value.create()))
				.toArray(Named[]::new);

			return () -> {
				MapSnapshot.Builder builder = MapSnapshot.builder();
				for(Named<Sampler<?>> value : samplers)
				{
					builder.set(value.name, value.value.sample());
				}
				return builder.build();
			};
		}
	}
}
