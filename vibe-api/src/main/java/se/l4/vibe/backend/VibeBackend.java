package se.l4.vibe.backend;

import se.l4.vibe.Vibe;
import se.l4.vibe.event.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.Sampler;
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
	void export(String path, Sampler<?> series);

	/**
	 * Export a probe.
	 *
	 * @param path
	 * @param probe
	 */
	void export(String path, Probe<?> probe);

	/**
	 * Export a collection of events.
	 *
	 * @param path
	 * @param events
	 */
	void export(String path, Events<?> events);

	/**
	 * Export a timer.
	 *
	 * @param path
	 * @param timer
	 */
	void export(String path, Timer timer);

	/**
	 * Release any resources held by this backend.
	 */
	void close();
}
