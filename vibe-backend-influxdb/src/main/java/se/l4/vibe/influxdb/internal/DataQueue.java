package se.l4.vibe.influxdb.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Queue for data points that are going to be sent to InfluxDB.
 */
public class DataQueue
{
	private final List<String> items;
	private final Consumer<String> flusher;
	private final ScheduledFuture<?> future;
	private final Lock lock;
	private final ScheduledExecutorService executor;

	public DataQueue(Consumer<String> flusher, ScheduledExecutorService executor)
	{
		this.flusher = flusher;
		this.executor = executor;

		items = new ArrayList<>(100);
		lock = new ReentrantLock();

		future = executor.scheduleAtFixedRate(this::flush, 5, 5, TimeUnit.SECONDS);
	}

	public void add(DataPoint point)
	{
		lock.lock();
		try
		{
			items.add(point.toLine());
			if(items.size() == 100)
			{
				String[] lines = items.toArray(new String[items.size()]);
				executor.execute(() -> send(lines, 1));
				items.clear();
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	private void send(String[] lines, int attempt)
	{
		try
		{
			StringBuilder builder = new StringBuilder();
			for(String line : lines)
			{
				builder.append(line).append("\n");
			}

			flusher.accept(builder.toString());
		}
		catch(Exception e)
		{
			// TODO: Support retrying sending
		}
	}

	/**
	 * Flush this queue.
	 */
	public void flush()
	{
		lock.lock();
		try
		{
			String[] lines = items.toArray(new String[items.size()]);
			executor.execute(() -> send(lines, 1));
			items.clear();
		}
		finally
		{
			lock.unlock();
		}
	}

	public void close()
	{
		flush();
		future.cancel(true);
	}
}
