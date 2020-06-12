package se.l4.vibe;

import se.l4.vibe.checks.Check;
import se.l4.vibe.events.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.Sampler;
import se.l4.vibe.timers.Timer;

/**
 * Backend for a {@link Vibe} instance.
 */
public interface VibeBackend
{
	/**
	 * Export a {@link Sampler}.
	 *
	 * @param path
	 * @param series
	 */
	default Handle export(String path, Sampler<?> series)
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
