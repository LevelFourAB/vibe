package se.l4.vibe;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.l4.vibe.events.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.SampledProbe;
import se.l4.vibe.sampling.Sampler;
import se.l4.vibe.timers.Timer;

public class VibeTest
{
	private TestBackend backend;
	private Vibe vibe;

	@Before
	public void beforeTest()
	{
		backend = new TestBackend();
		vibe = Vibe.builder()
			.addBackend(backend)
			.build();
	}

	@After
	public void afterTest()
	{
		vibe.destroy();
	}

	@Test
	public void testExportProbe()
	{
		Probe<Double> randomProbe = () -> ThreadLocalRandom.current().nextDouble();

		Export<?> export = vibe.export(randomProbe)
			.at("probe")
			.done();

		assertThat(backend.get("probe"), is(randomProbe));

		export.remove();

		assertThat(backend.get("probe"), is(nullValue()));
	}

	@Test
	public void testExportSampler()
	{
		Sampler<?> sampler = Sampler.forProbe(() -> ThreadLocalRandom.current().nextDouble())
			.build();

		Export<?> export = vibe.export(sampler)
			.at("sampler")
			.done();

		assertThat(backend.get("sampler"), is(sampler));

		export.remove();

		assertThat(backend.get("sampler"), is(nullValue()));
	}

	@Test
	public void testExportSampledProbe()
	{
		SampledProbe<Double> probe = () -> ThreadLocalRandom.current().nextDouble();

		Export<?> export = vibe.export(probe)
			.at("sampler")
			.done();

		assertThat(backend.get("sampler"), is(instanceOf(Sampler.class)));

		export.remove();

		assertThat(backend.get("sampler"), is(nullValue()));
	}

	@Test
	public void testExportTimer()
	{
		Timer timer = Timer.builder()
			.build();

		Export<?> export = vibe.export(timer)
			.at("timer")
			.done();

		assertThat(backend.get("timer"), is(timer));

		export.remove();

		assertThat(backend.get("timer"), is(nullValue()));
	}

	@Test
	public void testExportEvents()
	{
		Events<String> events = Events.<String>builder()
			.build();

		Export<?> export = vibe.export(events)
			.at("events")
			.done();

		assertThat(backend.get("events"), is(events));

		export.remove();

		assertThat(backend.get("events"), is(nullValue()));
	}

	@Test
	public void testDestroyReleasesHandles()
	{
		Probe<Double> randomProbe = () -> ThreadLocalRandom.current().nextDouble();

		vibe.export(randomProbe)
			.at("probe")
			.done();

		vibe.destroy();

		assertThat(backend.get("probe"), is(nullValue()));
	}

	@Test
	public void testScopedVibeProbe()
	{
		Probe<Double> randomProbe = () -> ThreadLocalRandom.current().nextDouble();

		Vibe scoped = vibe.scope("scoped");
		scoped.export(randomProbe)
			.at("probe")
			.done();

		assertThat(backend.get("scoped/probe"), is(randomProbe));
	}

	@Test
	public void testScopedVibeDestroyReleasesHandles()
	{
		Probe<Double> randomProbe = () -> ThreadLocalRandom.current().nextDouble();

		Vibe scoped = vibe.scope("scoped");
		scoped.export(randomProbe)
			.at("probe")
			.done();

		assertThat(backend.get("scoped/probe"), is(randomProbe));

		scoped.destroy();

		assertThat(backend.get("scoped/probe"), is(nullValue()));
	}

	@Test
	public void testScopedVibeWithoutPath()
	{
		Probe<Double> randomProbe = () -> ThreadLocalRandom.current().nextDouble();

		Vibe scoped = vibe.scope();
		scoped.export(randomProbe)
			.at("probe")
			.done();

		assertThat(backend.get("probe"), is(randomProbe));

		scoped.destroy();

		assertThat(backend.get("probe"), is(nullValue()));
	}

	public static class TestBackend
		implements VibeBackend
	{
		private final Map<String, Object> exported;

		public TestBackend()
		{
			exported = new HashMap<>();
		}

		public Object get(String name)
		{
			return exported.get(name);
		}

		@Override
		public void close()
		{
		}

		private Handle export0(String path, Object o)
		{
			exported.put(path, o);
			return () -> exported.remove(path);
		}

		@Override
		public Handle export(String path, Events<?> events)
		{
			return export0(path, events);
		}

		@Override
		public Handle export(String path, Probe<?> probe)
		{
			return export0(path, probe);
		}

		@Override
		public Handle export(String path, Sampler<?> series)
		{
			return export0(path, series);
		}

		@Override
		public Handle export(String path, Timer timer)
		{
			return export0(path, timer);
		}
	}
}
