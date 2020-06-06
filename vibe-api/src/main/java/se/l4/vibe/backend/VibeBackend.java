package se.l4.vibe.backend;

import se.l4.vibe.Vibe;
import se.l4.vibe.event.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.Sampler;
import se.l4.vibe.timer.Timer;

/**
 * Backend for a {@link Vibe} instance.
 *
 * @author Andreas Holstenson
 *
 */
public interface VibeBackend
{
	/**
	 * Export a time series.
	 *
	 * @param path
	 * @param series
	 */
	Handle export(String path, Sampler<?> series);

	/**
	 * Export a probe.
	 *
	 * @param path
	 * @param probe
	 */
	Handle export(String path, Probe<?> probe);

	/**
	 * Export a collection of events.
	 *
	 * @param path
	 * @param events
	 */
	Handle export(String path, Events<?> events);

	/**
	 * Export a timer.
	 *
	 * @param path
	 * @param timer
	 */
	Handle export(String path, Timer timer);

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
