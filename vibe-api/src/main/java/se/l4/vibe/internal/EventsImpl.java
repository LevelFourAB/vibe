package se.l4.vibe.internal;

import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

import se.l4.vibe.Handle;
import se.l4.vibe.events.Event;
import se.l4.vibe.events.EventData;
import se.l4.vibe.events.EventListener;
import se.l4.vibe.events.EventSeverity;
import se.l4.vibe.events.Events;
import se.l4.vibe.operations.Change;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.SampledProbe;

/**
 * Implementation of {@link Events}.
 *
 * @param <T>
 */
public class EventsImpl<T extends EventData>
	implements Events<T>
{
	private final Listeners<EventListener<T>> listeners;
	private final EventSeverity severity;

	private final LongAdder totalEvents;

	public EventsImpl(EventSeverity severity)
	{
		this.severity = severity;

		listeners = new Listeners<>();

		totalEvents = new LongAdder();
	}

	public void register(T eventData)
	{
		register(severity, eventData);
	}

	public void register(EventSeverity severity, T eventData)
	{
		totalEvents.increment();

		Event<T> event = new Event<>(severity, eventData);
		listeners.forEach(l -> l.eventRegistered(event));
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
		return totalEvents::longValue;
	}

	@Override
	public SampledProbe<Long> getEventsProbe()
	{
		return SampledProbe.over(getTotalEventsProbe())
			.apply(Change.changeAsLong());
	}

	public static class BuilderImpl<T extends EventData>
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
