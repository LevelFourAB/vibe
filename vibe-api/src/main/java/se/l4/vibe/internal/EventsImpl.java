package se.l4.vibe.internal;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import se.l4.vibe.Handle;
import se.l4.vibe.events.EventListener;
import se.l4.vibe.events.EventSeverity;
import se.l4.vibe.events.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.SampledProbe;

/**
 * Implementation of {@link Events}.
 *
 * @param <T>
 */
public class EventsImpl<T>
	implements Events<T>
{
	private final Listeners<EventListener<T>> listeners;
	private final EventSeverity severity;

	private final AtomicLong totalEvents;

	public EventsImpl(EventSeverity severity)
	{
		this.severity = severity;

		listeners = new Listeners<>();

		totalEvents = new AtomicLong();
	}

	public void register(T event)
	{
		register(severity, event);
	}

	public void register(EventSeverity severity, T event)
	{
		totalEvents.incrementAndGet();

		listeners.forEach(l -> l.eventRegistered(this, severity, event));
	}

	@Override
	public EventSeverity getDefaultSeverity()
	{
		return severity;
	}

	@Override
	public Handle addListener(EventListener<T> listener)
	{
		return listeners.add(listener);
	}

	@Override
	public void removeListener(EventListener<T> listener)
	{
		listeners.remove(listener);
	}

	@Override
	public Probe<Long> getTotalEventsProbe()
	{
		return new Probe<Long>()
		{
			@Override
			public Long read()
			{
				return totalEvents.longValue();
			}
		};
	}

	@Override
	public SampledProbe<Long> getEventsProbe()
	{
		return new SampledProbe<Long>()
		{
			private long lastValue;

			@Override
			public Long sample()
			{
				long current = totalEvents.longValue();
				long sinceLastSample = current - lastValue;
				lastValue = current;

				return sinceLastSample;
			}
		};
	}

	public static class BuilderImpl<T>
		implements Builder<T>
	{
		private EventSeverity severity;

		public BuilderImpl()
		{
			this.severity = EventSeverity.INFO;
		}

		@Override
		public Builder<T> withSeverity(EventSeverity severity)
		{
			Objects.requireNonNull(severity);

			return this;
		}

		@Override
		public Events<T> build()
		{
			return new EventsImpl<>(severity);
		}
	}
}
