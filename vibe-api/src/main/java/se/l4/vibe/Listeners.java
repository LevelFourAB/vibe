package se.l4.vibe;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class Listeners<T>
{
	private static final Object[] EMPTY = new Object[0];

	private final OnChange onListenersChanged;

	private final Lock listenersLock;
	private volatile Object[] listeners;

	public Listeners()
	{
		this(null);
	}

	public Listeners(OnChange onListenersChanged)
	{
		listenersLock = new ReentrantLock();
		listeners = EMPTY;

		this.onListenersChanged = onListenersChanged;
	}

	public int getSize()
	{
		return listeners.length;
	}

	public ListenerHandle add(T listener)
	{
		listenersLock.lock();
		try
		{
			Object[] listeners = this.listeners;
			Object[] result = new Object[listeners.length + 1];
			System.arraycopy(listeners, 0, result, 0, listeners.length);
			result[listeners.length] = listener;

			this.listeners = result;

			if(onListenersChanged != null)
			{
				onListenersChanged.listenersChanged(result.length);
			}

			return () -> remove(listener);
		}
		finally
		{
			listenersLock.unlock();
		}
	}

	public void remove(T listener)
	{
		listenersLock.lock();
		try
		{
			Object[] listeners = this.listeners;
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
				// Nothing to do, no such listener
				return;
			}

			int length = listeners.length;
			Object[] result = new Object[length - 1];
			System.arraycopy(listeners, 0, result, 0, index);

			if(index < length - 1)
			{
				System.arraycopy(listeners, index + 1, result, index, length - index - 1);
			}

			this.listeners = result;

			if(onListenersChanged != null)
			{
				onListenersChanged.listenersChanged(result.length);
			}
		}
		finally
		{
			listenersLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	public void forEach(Consumer<? super T> action)
	{
		Object[] listeners = this.listeners;
		for(Object o : listeners)
		{
			action.accept((T) o);
		}
	}

	public interface OnChange
	{
		void listenersChanged(int listenerCount);
	}
}
