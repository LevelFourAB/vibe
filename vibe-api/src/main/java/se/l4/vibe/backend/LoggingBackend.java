package se.l4.vibe.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.vibe.Handle;
import se.l4.vibe.Vibe;
import se.l4.vibe.event.EventListener;
import se.l4.vibe.event.EventSeverity;
import se.l4.vibe.event.Events;
import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.SampleListener;
import se.l4.vibe.sampling.Sampler;

/**
 * Backed that can log events and samples to a {@link Logger}. Use
 * {@link #builder()} to start building an instance of this backend.
 *
 * <p>
 * <pre>
 * LoggingBackend backend = LoggingBackend.builder()
 *   .logEvents()
 *   .build();
 * </pre>
 */
public class LoggingBackend
	implements VibeBackend
{
	private final Logger logger;
	private final boolean logSamples;
	private final boolean logEvents;

	private LoggingBackend(
		Logger logger,
		boolean logSamples,
		boolean logEvents
	)
	{
		this.logger = logger;
		this.logSamples = logSamples;
		this.logEvents = logEvents;
	}

	@Override
	public Handle export(String path, Sampler<?> series)
	{
		if(! logSamples) return Handle.empty();

		SampleListener listener = new PrintSampleListener(logger, path);
		return series.addListener(listener);
	}

	@Override
	public Handle export(String path, Events<?> events)
	{
		if(! logEvents) return Handle.empty();

		EventListener listener = new PrintEventListener(logger, path);
		return events.addListener(listener);
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public static class Builder
	{
		private Logger logger;
		private boolean logEvents;
		private boolean logSamples;

		public Builder setLogger(Logger logger)
		{
			this.logger = logger;
			return this;
		}

		public Builder setLogger(String name)
		{
			this.logger = LoggerFactory.getLogger(name);
			return this;
		}

		public Builder setLogger(Class<?> type)
		{
			this.logger = LoggerFactory.getLogger(type);
			return this;
		}

		public Builder logEvents()
		{
			this.logEvents = true;
			return this;
		}

		public Builder logSamples()
		{
			this.logSamples = true;
			return this;
		}

		public LoggingBackend build()
		{
			if(logger == null)
			{
				logger = LoggerFactory.getLogger(Vibe.class);
			}

			return new LoggingBackend(
				logger,
				logSamples,
				logEvents
			);
		}
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
		public void sampleAcquired(Sample entry)
		{
			logger.info("{}: {}", path, entry.getValue());
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
		public void eventRegistered(Events events, EventSeverity severity, Object event)
		{
			switch(severity)
			{
				case DEBUG:
					logger.debug("{}: {}", path, event);
					break;
				case INFO:
					logger.info("{}: {}", path, event);
					break;
				case WARN:
					logger.warn("{}: {}", path, event);
					break;
				case ERROR:
					logger.error("{}: {}", path, event);
					break;
				case CRITICAL:
					logger.error("CRITICAL: {}: {}", path, event);
					break;
			}
		}
	}

	@Override
	public void close()
	{
	}
}
