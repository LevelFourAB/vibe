package se.l4.vibe.probes;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract implementation of {@link TimeSeries}.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public abstract class AbstractTimeSeries<T>
	implements TimeSeries<T>
{
	private static final SampleListener[] EMPTY = new SampleListener[0];
	
	private final Lock listenerLock;
	protected volatile SampleListener<T>[] listeners;
	
	public AbstractTimeSeries()
	{
		listenerLock = new ReentrantLock();
		listeners = EMPTY;
	}
	
	@Override
	public void addListener(SampleListener<T> listener)
	{
		listenerLock.lock();
		try
		{
			SampleListener<T>[] listeners = this.listeners;
			SampleListener<T>[] newListeners = new SampleListener[listeners.length + 1];
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
	public void removeListener(SampleListener<T> listener)
	{
		listenerLock.lock();
		try
		{
			SampleListener<T>[] listeners = this.listeners;
			
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
			
			SampleListener<T>[] newListeners = new SampleListener[listeners.length - 1];
			
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
