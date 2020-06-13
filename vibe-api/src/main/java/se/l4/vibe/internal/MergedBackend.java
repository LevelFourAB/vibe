package se.l4.vibe.internal;

import java.util.Arrays;

import se.l4.vibe.Handle;
import se.l4.vibe.VibeBackend;
import se.l4.vibe.checks.Check;
import se.l4.vibe.events.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.SampledProbe;
import se.l4.vibe.sampling.TimeSampler;
import se.l4.vibe.timers.Timer;

/**
 * Collection of several backends that are triggered in order.
 */
public class MergedBackend
	implements VibeBackend
{
	private final VibeBackend[] backends;

	public MergedBackend(VibeBackend... backends)
	{
		this.backends = backends;
	}

	@Override
	public Handle export(String path, TimeSampler<?> series)
	{
		Handle[] handles = Arrays.stream(backends)
			.map(b -> b.export(path, series))
			.toArray(Handle[]::new);

		return new MergedHandle(handles);
	}

	@Override
	public Handle export(String path, Probe<?> probe)
	{
		Handle[] handles = Arrays.stream(backends)
			.map(b -> b.export(path, probe))
			.toArray(Handle[]::new);

		return new MergedHandle(handles);
	}

	@Override
	public Handle export(String path, SampledProbe<?> probe)
	{
		Handle[] handles = Arrays.stream(backends)
			.map(b -> b.export(path, probe))
			.toArray(Handle[]::new);

		return new MergedHandle(handles);
	}

	@Override
	public Handle export(String path, Events<?> events)
	{
		Handle[] handles = Arrays.stream(backends)
			.map(b -> b.export(path, events))
			.toArray(Handle[]::new);

		return new MergedHandle(handles);
	}

	@Override
	public Handle export(String path, Timer timer)
	{
		Handle[] handles = Arrays.stream(backends)
			.map(b -> b.export(path, timer))
			.toArray(Handle[]::new);

		return new MergedHandle(handles);
	}

	@Override
	public Handle export(String path, Check check)
	{
		Handle[] handles = Arrays.stream(backends)
			.map(b -> b.export(path, check))
			.toArray(Handle[]::new);

		return new MergedHandle(handles);
	}

	@Override
	public void close()
	{
		for(VibeBackend backend : backends)
		{
			backend.close();
		}
	}

	private static class MergedHandle
		implements Handle
	{
		private final Handle[] handles;

		public MergedHandle(Handle[] handles)
		{
			this.handles = handles;
		}

		@Override
		public void release()
		{
			for(Handle h : handles)
			{
				h.release();
			}
		}
	}
}
