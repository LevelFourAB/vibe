package se.l4.vibe.internal.timer;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import se.l4.vibe.percentile.PercentileCounter;
import se.l4.vibe.timer.Stopwatch;
import se.l4.vibe.timer.Timer;
import se.l4.vibe.timer.TimerListener;
import se.l4.vibe.timer.TimerSnapshot;

/**
 * Implementation of {@link Timer}.
 *
 * @author Andreas Holstenson
 *
 */
public class TimerImpl
	implements Timer
{
	private static final TimerListener[] EMPTY = new TimerListener[0];

	private final Lock listenerLock;
	protected volatile TimerListener[] listeners;

	private final PercentileCounter counter;
	private volatile TimerSnapshot lastSample;

	private final AtomicLong min;
	private final AtomicLong max;

	public TimerImpl(PercentileCounter counter)
	{
		this.counter = counter;

		listenerLock = new ReentrantLock();
		listeners = EMPTY;

		min = new AtomicLong();
		max = new AtomicLong();
	}

	@Override
	public void addListener(TimerListener listener)
	{
		listenerLock.lock();
		try
		{
			TimerListener[] listeners = this.listeners;
			TimerListener[] newListeners = new TimerListener[listeners.length + 1];
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
	public void removeListener(TimerListener listener)
	{
		listenerLock.lock();
		try
		{
			TimerListener[] listeners = this.listeners;

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

			TimerListener[] newListeners = new TimerListener[listeners.length - 1];

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

	private long time()
	{
		return System.nanoTime();
	}

	@Override
	public Stopwatch start()
	{
		final long time = time();
		return new Stopwatch()
		{
			@Override
			public void stop()
			{
				long now = time();
				long nowInMs = System.currentTimeMillis();

				long total = now - time;
				counter.add(total);
				min.updateAndGet(c -> c > total ? total : c);
				max.updateAndGet(c -> c < total ? total : c);

				TimerListener[] listeners = TimerImpl.this.listeners;
				if(listeners.length > 0)
				{
					for(TimerListener l : listeners)
					{
						l.timerEvent(nowInMs, total);
					}
				}
			}
		};
	}

	private TimerSnapshot createSample()
	{
		return new TimerSnapshotImpl(counter.get(), min.get(), max.get());
	}

	@Override
	public TimerSnapshot peek()
	{
		return createSample();
	}

	@Override
	public TimerSnapshot read()
	{
		return lastSample;
	}

	@Override
	public TimerSnapshot sample()
	{
		lastSample = createSample();
		counter.reset();
		min.set(Long.MAX_VALUE);
		max.set(0);
		return lastSample;
	}
}
