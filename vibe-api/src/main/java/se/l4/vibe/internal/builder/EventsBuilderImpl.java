package se.l4.vibe.internal.builder;

import se.l4.vibe.backend.VibeBackend;
import se.l4.vibe.builder.EventsBuilder;
import se.l4.vibe.event.EventSeverity;
import se.l4.vibe.event.Events;
import se.l4.vibe.internal.EventsImpl;

/**
 * Builder for events.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class EventsBuilderImpl<T>
	extends AbstractBuilder<EventsBuilder<T>>
	implements EventsBuilder<T>
{
	private final VibeBackend backend;
	
	private EventSeverity severity;

	public EventsBuilderImpl(VibeBackend backend)
	{
		this.backend = backend;
		severity = EventSeverity.INFO;
	}
	
	@Override
	public EventsBuilder<T> setSeverity(EventSeverity severity)
	{
		this.severity = severity;
		
		return this;
	}
	
	@Override
	public Events<T> build()
	{
		return new EventsImpl<T>(severity);
	}
	
	@Override
	public Events<T> export()
	{
		verify();
		
		Events<T> events = build();
		backend.export(path, events);
		
		return events;
	}
}