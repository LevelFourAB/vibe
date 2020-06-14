package se.l4.vibe;

import java.util.concurrent.ThreadPoolExecutor;

import edu.umd.cs.findbugs.annotations.NonNull;
import se.l4.vibe.operations.Change;
import se.l4.vibe.operations.OperationExecutor;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.snapshots.KeyValueReceiver;
import se.l4.vibe.snapshots.Snapshot;

/**
 * Probes that can be used for {@link ThreadPoolExecutor}.
 */
public class ExecutorProbes
{
	private ExecutorProbes()
	{
	}

	@NonNull
	public static SampledProbe<ThreadPoolExecutorSnapshot> forThreadPoolExecutor(
		@NonNull ThreadPoolExecutor executor
	)
	{
		return () -> {
			OperationExecutor<Number, Long> completedTasksChange = Change.changeAsLong().create();

			return () -> new ThreadPoolExecutorSnapshot(
				executor.getPoolSize(),
				executor.getMaximumPoolSize(),
				executor.getActiveCount(),
				completedTasksChange.apply(executor.getCompletedTaskCount()),
				executor.getQueue().size()
			);
		};
	}

	public static class ThreadPoolExecutorSnapshot
		implements Snapshot
	{
		private final long poolSize;
		private final long maxPoolSize;
		private final long active;
		private final long completedTasks;
		private final long queueSize;

		public ThreadPoolExecutorSnapshot(
			long size,
			long maxSize,
			long active,
			long completedTasks,
			long queueSize
		)
		{
			this.poolSize = size;
			this.maxPoolSize = maxSize;
			this.active = active;
			this.completedTasks = completedTasks;
			this.queueSize = queueSize;
		}

		@Override
		public void mapToKeyValues(KeyValueReceiver receiver)
		{
			receiver.add("poolSize", poolSize);
			receiver.add("maxPoolSize", maxPoolSize);
			receiver.add("activeTasks", active);
			receiver.add("completedTasks", completedTasks);
			receiver.add("queueSize", queueSize);
		}
	}
}
