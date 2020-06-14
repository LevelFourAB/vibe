package se.l4.vibe;

import se.l4.vibe.checks.Check;
import se.l4.vibe.events.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.sampling.TimeSampler;
import se.l4.vibe.timers.Timer;

/**
 * Backend for a {@link Vibe} instance.
 */
public interface VibeBackend
{
	/**
	 * Export a {@link TimeSampler}.
	 *
	 * @param path
	 * @param series
	 */
	default Handle export(String path, TimeSampler<?> series)
	{
		return Handle.empty();
	}

	/**
	 * Export a probe.
	 *
	 * @param path
	 * @param probe
	 */
	default Handle export(String path, Probe<?> probe)
	{
		return Handle.empty();
	}

	/**
	 * Export a probe that requires sampling.
	 *
	 * @param path
	 * @param probe
	 */
	default Handle export(String path, SampledProbe<?> probe)
	{
		return Handle.empty();
	}

	/**
	 * Export a collection of events.
	 *
	 * @param path
	 * @param events
	 */
	default Handle export(String path, Events<?> events)
	{
		return Handle.empty();
	}

	/**
	 * Export a timer.
	 *
	 * @param path
	 * @param timer
	 */
	default Handle export(String path, Timer timer)
	{
		return Handle.empty();
	}

	/**
	 * Export a check.
	 *
	 * @param path
	 * @param check
	 */
	default Handle export(String path, Check check)
	{
		return Handle.empty();
	}

	/**
	 * Release any resources held by this backend.
	 */
	void close();
}
