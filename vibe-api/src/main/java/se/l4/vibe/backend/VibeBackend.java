package se.l4.vibe.backend;

import se.l4.vibe.Vibe;
import se.l4.vibe.event.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.Sampler;
import se.l4.vibe.timer.Timer;

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
	 * Release any resources held by this backend.
	 */
	void close();

	/**
	 * Handle used to remove something that has been exported.
	 */
	@FunctionalInterface
	interface Handle
	{
		void remove();

		static Handle empty()
		{
			return () -> {};
		}
	}
}
