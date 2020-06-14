package se.l4.vibe;

import edu.umd.cs.findbugs.annotations.NonNull;
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
	@NonNull
	default Handle export(@NonNull String path, @NonNull TimeSampler<?> series)
	{
		return Handle.empty();
	}

	/**
	 * Export a probe.
	 *
	 * @param path
	 * @param probe
	 */
	@NonNull
	default Handle export(@NonNull String path, @NonNull Probe<?> probe)
	{
		return Handle.empty();
	}

	/**
	 * Export a probe that requires sampling.
	 *
	 * @param path
	 * @param probe
	 */
	@NonNull
	default Handle export(@NonNull String path, @NonNull SampledProbe<?> probe)
	{
		return Handle.empty();
	}

	/**
	 * Export a collection of events.
	 *
	 * @param path
	 * @param events
	 */
	@NonNull
	default Handle export(@NonNull String path, @NonNull Events<?> events)
	{
		return Handle.empty();
	}

	/**
	 * Export a timer.
	 *
	 * @param path
	 * @param timer
	 */
	@NonNull
	default Handle export(@NonNull String path, @NonNull Timer timer)
	{
		return Handle.empty();
	}

	/**
	 * Export a check.
	 *
	 * @param path
	 * @param check
	 */
	@NonNull
	default Handle export(@NonNull String path, @NonNull Check check)
	{
		return Handle.empty();
	}

	/**
	 * Release any resources held by this backend.
	 */
	void close();
}
