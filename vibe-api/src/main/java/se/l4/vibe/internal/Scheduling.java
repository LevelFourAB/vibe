package se.l4.vibe.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import se.l4.vibe.Handle;

/**
 * Internal class for scheduling things in Vibe. Will manage a small executor
 * that things like sampling run on in Vibe.
 */
public class Scheduling
{
	private static ScheduledExecutorService executor;
	private static final AtomicInteger samplers;

	static
	{
		samplers = new AtomicInteger();
	}

	private Scheduling()
	{
	}

	private static synchronized ScheduledExecutorService startExecutor()
	{
		if(executor != null) return executor;

		executor = Executors.newScheduledThreadPool(4, new ThreadFactory()
		{
			private final AtomicInteger counter = new AtomicInteger();

			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread(r, "vibe-" + counter.incrementAndGet());
				t.setDaemon(true);
				return t;
			}
		});

		return executor;
	}

	private static synchronized void stopExecutor()
	{
		executor.shutdown();
		executor = null;
	}

	public static Handle scheduleSampling(long sampleIntervalInMs, Runnable action)
	{
		if(executor == null)
		{
			startExecutor();
		}

		long initialDelay = sampleIntervalInMs - (System.currentTimeMillis() % sampleIntervalInMs);
		ScheduledFuture<?> future = executor.scheduleAtFixedRate(
			action,
			initialDelay,
			sampleIntervalInMs,
			TimeUnit.MILLISECONDS
		);
		samplers.incrementAndGet();

		return () -> {
			if(future.isCancelled()) return;

			future.cancel(false);
			if(samplers.decrementAndGet() == 0)
			{
				stopExecutor();
			}
		};
	}
}
