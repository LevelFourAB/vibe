package se.l4.vibe.internal;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import se.l4.vibe.event.EventListener;
import se.l4.vibe.event.EventSeverity;
import se.l4.vibe.event.Events;

/**
 * Implementation of {@link Events}.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class EventsImpl<T>
	implements Events<T>
{
	private static final EventListener[] EMPTY = new EventListener[0];
	
	private final EventSeverity severity;
	
	private final Lock listenerLock;
	protected volatile EventListener<T>[] listeners;
	
	public EventsImpl(EventSeverity severity)
	{
		this.severity = severity;
		
		listenerLock = new ReentrantLock();
		listeners = EMPTY;
	}
	
	public void register(T event)
	{
		EventListener<T>[] listeners = this.listeners;
		for(EventListener<T> listener : listeners)
		{
			listener.eventRegistered(this, severity, event);
		}
	}
	
	public void register(EventSeverity severity, T event)
	{
		EventListener<T>[] listeners = this.listeners;
		for(EventListener<T> listener : listeners)
		{
			listener.eventRegistered(this, severity, event);
		}
	}
	
	@Override
	public EventSeverity getDefaultSeverity()
	{
		return severity;
	}
	
	@Override
	public void addListener(EventListener<T> listener)
	{
		listenerLock.lock();
		try
		{
			EventListener<T>[] listeners = this.listeners;
			EventListener<T>[] newListeners = new EventListener[listeners.length + 1];
			System.arraycopy(listeners, 0, newListeners, 0, listeners.length);
			newListeners[listeners.length] = listener;
			
			this.listeners = newListeners;
		}
		finally
		{
			listenerLock.unlock();
		}
	}
	
	@Override
	public void removeListener(EventListener<T> listener)
	{
		listenerLock.lock();
		try
		{
			EventListener<T>[] listeners = this.listeners;
			
			int index = -1;
			for(int i=0, n=listeners.length; i<n; i++)
			{
				if(listeners[i] == listener)
				{
					index = i;
					break;
				}
			}
			
			if(index == -1)
			{
				// No such listener, just return
				return;
			}
			
			EventListener<T>[] newListeners = new EventListener[listeners.length - 1];
			
			System.arraycopy(listeners, 0, newListeners, 0, index);
			if(index < listeners.length - 1)
	        {
	        	System.arraycopy(listeners, index + 1, newListeners, index, listeners.length- index - 1);
	        }
	        
			this.listeners = newListeners;
		}
		finally
		{
			listenerLock.unlock();
		}
	}

}
