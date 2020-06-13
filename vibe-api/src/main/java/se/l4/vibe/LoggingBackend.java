package se.l4.vibe;

import java.time.Duration;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.vibe.events.Event;
import se.l4.vibe.events.EventListener;
import se.l4.vibe.events.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.SampleListener;
import se.l4.vibe.sampling.SampledProbe;
import se.l4.vibe.sampling.TimeSampler;

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
	private final Duration samplingInterval;
	private final Logger logger;
	private final boolean logSamples;
	private final boolean logEvents;

	private LoggingBackend(
		Duration samplingInterval,
		Logger logger,
		boolean logSamples,
		boolean logEvents
	)
	{
		this.samplingInterval = samplingInterval;
		this.logger = logger;
		this.logSamples = logSamples;
		this.logEvents = logEvents;
	}

	@Override
	public Handle export(String path, TimeSampler<?> series)
	{
		if(! logSamples) return Handle.empty();

		SampleListener listener = new PrintSampleListener(logger, path);
		return series.addListener(listener);
	}

	@Override
	public Handle export(String path, Probe<?> probe)
	{
		return sampleAndExport(path, SampledProbe.over(probe));
	}

	@Override
	public Handle export(String path, SampledProbe<?> probe)
	{
		return sampleAndExport(path, probe);
	}

	private Handle sampleAndExport(String path, SampledProbe<?> probe)
	{
		if(! logSamples) return Handle.empty();

		TimeSampler<?> sampler = TimeSampler.forProbe(probe)
			.withInterval(samplingInterval)
			.build();

		return export(path, sampler);
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
		private Duration samplingInterval;
		private Logger logger;
		private boolean logEvents;
		private boolean logSamples;

		public Builder()
		{
			logger = LoggerFactory.getLogger(Vibe.class);
			samplingInterval = Duration.ofSeconds(10);
		}

		/**
		 * Set the sampling interval this backend should use for {@link Probe}s
		 * and {@link SampledProbe}s.
		 *
		 * @param interval
		 *   interval to use
		 * @return
		 */
		public Builder withSamplingInterval(Duration interval)
		{
			Objects.requireNonNull(interval, "interval can not be null");
			this.samplingInterval = interval;

			return this;
		}

		public Builder withLogger(Logger logger)
		{
			Objects.requireNonNull(logger, "logger may not be null");
			this.logger = logger;

			return this;
		}

		public Builder withLogger(String name)
		{
			Objects.requireNonNull(logger, "name may not be null");
			this.logger = LoggerFactory.getLogger(name);

			return this;
		}

		public Builder withLogger(Class<?> type)
		{
			Objects.requireNonNull(logger, "type may not be null");
			this.logger = LoggerFactory.getLogger(type);

			return this;
		}

		public Builder logEvents()
		{
			return logEvents(true);
		}

		public Builder logEvents(boolean enabled)
		{
			this.logEvents = enabled;
			return this;
		}

		public Builder logSamples()
		{
			return logSamples(true);
		}

		public Builder logSamples(boolean enabled)
		{
			this.logSamples = enabled;
			return this;
		}

		public LoggingBackend build()
		{
			return new LoggingBackend(
				samplingInterval,
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
		public void eventRegistered(Event event)
		{
			String message = event.getData().toHumanReadable();
			switch(event.getSeverity())
			{
				case DEBUG:
					logger.debug("{}: {}", path, message);
					break;
				case INFO:
					logger.info("{}: {}", path, message);
					break;
				case WARN:
					logger.warn("{}: {}", path, message);
					break;
				case ERROR:
					logger.error("{}: {}", path, message);
					break;
				case CRITICAL:
					logger.error("CRITICAL: {}: {}", path, message);
					break;
			}
		}
	}

	@Override
	public void close()
	{
	}
}
