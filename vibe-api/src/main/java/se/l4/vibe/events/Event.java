package se.l4.vibe.events;

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
		EventSeverity severity,
		T data
	)
	{
		this.severity = severity;
		this.data = data;
	}

	public EventSeverity getSeverity()
	{
		return severity;
	}

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
