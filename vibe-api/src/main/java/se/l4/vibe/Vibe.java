package se.l4.vibe;


import se.l4.vibe.backend.VibeBackend;
import se.l4.vibe.builder.EventsBuilder;
import se.l4.vibe.builder.ProbeBuilder;
import se.l4.vibe.builder.TimeSeriesBuilder;
import se.l4.vibe.builder.TimerBuilder;
import se.l4.vibe.event.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.probes.TimeSeries;
import se.l4.vibe.timer.Timer;

/**
 * Main interface for statistics and events.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Vibe
{
	/**
	 * Export a new probe.
	 * 
	 * @param probe
	 * @return
	 */
	<T> ProbeBuilder<T> probe(Probe<T> probe);

	/**
	 * Start creating a new time series.
	 * 
	 * @return
	 */
	<T> TimeSeriesBuilder<T> timeSeries(SampledProbe<T> probe);
	
	/**
	 * Create a new events instance.
	 * 
	 * @param base
	 * @return
	 */
	<T> EventsBuilder<T> events(Class<T> base);
	
	/**
	 * Start creating a new timer.
	 * 
	 * @return
	 */
	TimerBuilder timer();
	
	/**
	 * Get a {@link Probe} that has been registered at the given path.
	 * 
	 * @param path
	 * @return
	 */
	<T> Probe<T> getProbe(String path);
	
	/**
	 * Get a {@link TimeSeries} that has been registered at the given path.
	 * 
	 * @param path
	 * @return
	 */
	<T> TimeSeries<T> getTimeSeries(String path);
	
	/**
	 * Get a {@link Events} that has been registered at the given path.
	 * 
	 * @param path
	 * @return
	 */
	<T> Events<T> getEvents(String path);
	
	/**
	 * Get a {@link Timer} that has been registered at the given path.
	 * 
	 * @param path
	 * @return
	 */
	Timer getTimer(String path);
	
	/**
	 * Register a new backend to this instance.
	 * 
	 * @param backend
	 */
	void addBackend(VibeBackend backend);
}
