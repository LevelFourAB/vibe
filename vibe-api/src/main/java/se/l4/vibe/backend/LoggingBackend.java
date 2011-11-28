package se.l4.vibe.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.vibe.Vibe;
import se.l4.vibe.event.EventListener;
import se.l4.vibe.event.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampleListener;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.probes.TimeSeries;

/**
 * Basic backend that will log a message every time something is sampled.
 * 
 * @author Andreas Holstenson
 *
 */
public class LoggingBackend
	implements VibeBackend
{
	private final Logger logger;
	
	public LoggingBackend()
	{
		this(Vibe.class);
	}
	
	public LoggingBackend(String root)
	{
		logger = LoggerFactory.getLogger(root);
	}
	
	public LoggingBackend(Class<?> root)
	{
		logger = LoggerFactory.getLogger(root);
	}

	@Override
	public void export(String path, TimeSeries<?> series)
	{
		series.addListener(new PrintSampleListener(logger, path));
	}
	
	@Override
	public void export(String path, Probe<?> probe)
	{
	}
	
	@Override
	public void export(String path, Events<?> events)
	{
		events.addListener(new PrintEventListener(logger, path));
	}

	private static class PrintSampleListener
		implements SampleListener
	{
		private final String path;
		private final Logger logger;

		public PrintSampleListener(Logger logger, String path)
		{
			this.logger = logger;
			this.path = path;
		}
		
		@Override
		public void sampleAcquired(SampledProbe probe, Object value)
		{
			logger.info("{}: {}", path, value);
		}
	}
	
	private static class PrintEventListener
		implements EventListener
	{
		private final String path;
		private final Logger logger;
	
		public PrintEventListener(Logger logger, String path)
		{
			this.logger = logger;
			this.path = path;
		}
		
		@Override
		public void eventRegistered(Events events, Object event)
		{
			logger.info("{}: {}", path, event);
		}
	}
}
