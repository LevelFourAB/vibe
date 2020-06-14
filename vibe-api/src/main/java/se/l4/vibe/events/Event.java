package se.l4.vibe.events;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Event as emitted by {@link Events}. Passed to {@link EventListener}.
 *
 * @param <T>
 */
public class Event<T extends EventData>
{
	private final EventSeverity severity;
	private final T data;

	public Event(
		@NonNull EventSeverity severity,
		@NonNull T data
	)
	{
		this.severity = severity;
		this.data = data;
	}

	@NonNull
	public EventSeverity getSeverity()
	{
		return severity;
	}

	@NonNull
	public T getData()
	{
		return data;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{severity=" + severity + ", data=" + data + "}";
	}
}
