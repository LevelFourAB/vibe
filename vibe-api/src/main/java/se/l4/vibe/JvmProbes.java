package se.l4.vibe;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

import se.l4.vibe.mapping.KeyValueMappable;
import se.l4.vibe.mapping.KeyValueReceiver;
import se.l4.vibe.mapping.KeyValueToString;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.SampledProbe;

/**
 * Probes for the JVM runtime.
 */
public class JvmProbes
{
	private JvmProbes()
	{
	}

	/**
	 * Get a probe for measuring the recent CPU usage of this JVM process as
	 * a factor between 0 to 1. A value of 0 would indicate that no CPUs are
	 * being used and 1 that all CPUs are being 100% used by the JVM.
	 *
	 * @return
	 */
	public static SampledProbe<Double> cpuUsage()
	{
		com.sun.management.OperatingSystemMXBean os =
			(com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

		return os::getProcessCpuLoad;
	}

	/**
	 * Get a probe for measuring the CPU usage of this JVM process as a factor
	 * between 0 to 1. A value of 0 would indicate that no CPUs are being used
	 * and 1 that all CPUs are being 100% used by the JVM.
	 *
	 * <p>
	 * This a {@link SampledProbe} that calculates the average CPU usage over
	 * the period between the previous sample and this sample.
	 *
	 * @return
	 */
	public static SampledProbe<Double> sampledCpuUsage()
	{
		com.sun.management.OperatingSystemMXBean os =
			(com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		Runtime rt = Runtime.getRuntime();

		return new SampledProbe<Double>()
		{
			private long lastUptime = runtime.getUptime();
			private long lastCpu = os.getProcessCpuTime();

			@Override
			public Double sample()
			{
				long uptime = runtime.getUptime();
				long cpu = os.getProcessCpuTime();

				long elapsedCpu = cpu - lastCpu;
				long elapsedTime = uptime - lastUptime;

				double v = Math.min(100, elapsedCpu / (elapsedTime * 10000.0 * rt.availableProcessors())) / 100;

				lastUptime = uptime;
				lastCpu = cpu;

				return v;
			}
		};
	}

	/**
	 * Get a probe that reads a snapshot of heap and non-heap memory usage.
	 *
	 * @return
	 *   snapshot of memory usage
	 * @see MemoryMXBean#getHeapMemoryUsage()
	 * @see MemoryMXBean#getNonHeapMemoryUsage()
	 */
	public static Probe<MemorySnapshot> memoryUsage()
	{
		MemoryMXBean b = ManagementFactory.getMemoryMXBean();

		return () -> {
			MemoryUsage heap = b.getHeapMemoryUsage();
			MemoryUsage nonHeap = b.getNonHeapMemoryUsage();

			return new MemorySnapshot(
				heap.getUsed(),
				heap.getCommitted(),
				heap.getMax(),
				nonHeap.getUsed(),
				nonHeap.getCommitted(),
				nonHeap.getMax()
			);
		};
	}

	/**
	 * Get a probe for monitoring heap memory usage within the JVM.
	 *
	 * @return
	 */
	public static Probe<Long> heapMemoryUsage()
	{
		MemoryMXBean b = ManagementFactory.getMemoryMXBean();
		return () -> b.getHeapMemoryUsage().getUsed();
	}

	/**
	 * Get a probe for monitoring non heap memory usage within the JVM.
	 *
	 * @return
	 */
	public static Probe<Long> nonHeapMemoryUsage()
	{
		MemoryMXBean b = ManagementFactory.getMemoryMXBean();
		return () -> b.getNonHeapMemoryUsage().getUsed();
	}

	/**
	 * Get a probe for memory usage within the JVM.
	 *
	 * @return
	 */
	public static Probe<Long> totalUsedMemory()
	{
		MemoryMXBean b = ManagementFactory.getMemoryMXBean();
		return () -> b.getHeapMemoryUsage().getUsed() + b.getNonHeapMemoryUsage().getUsed();
	}

	/**
	 * Get a probe for heap memory usage within the JVM.
	 *
	 * @return
	 */
	public static Probe<Double> heapMemoryAsFraction()
	{
		MemoryMXBean b = ManagementFactory.getMemoryMXBean();
		return () -> {
			MemoryUsage heap = b.getHeapMemoryUsage();
			return heap.getUsed() / (double) heap.getMax();
		};
	}

	/**
	 * Get details about the direct buffers allocated by the JVM.
	 *
	 * @return
	 */
	public static Probe<BufferPoolDetails> directBufferPool()
	{
		return bufferPool("direct");
	}

	/**
	 * Get details about the mapped buffers allocated by the JVM.
	 *
	 * @return
	 */
	public static Probe<BufferPoolDetails> mappedBufferPool()
	{
		return bufferPool("mapped");
	}

	private static Probe<BufferPoolDetails> bufferPool(String name)
	{
		for(BufferPoolMXBean b : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class))
		{
			if(b.getName().equals(name))
			{
				return () -> new BufferPoolDetails(b.getMemoryUsed(), b.getTotalCapacity(), b.getCount());
			}
		}

		throw new VibeException("Unknown buffer pool: " + name);
	}

	/**
	 * Probe that measures the uptime of the JVM in milliseconds.
	 *
	 * @return
	 */
	public static Probe<Long> uptime()
	{
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		return runtime::getUptime;
	}

	/**
	 * Get a probe suitable for sampling the number of threads active.
	 *
	 * @return
	 */
	public static Probe<Integer> threadCount()
	{
		ThreadMXBean thread = ManagementFactory.getThreadMXBean();
		return thread::getThreadCount;
	}

	/**
	 * Get the number of classes that are currently loaded by the JVM.
	 *
	 * @return
	 */
	public static Probe<Integer> loadedClassCount()
	{
		ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();
		return cl::getLoadedClassCount;
	}

	/**
	 * Get the number of open file descriptors. This will report the open
	 * file descriptors on UNIX-like systems, but will return {@code -1} for
	 * Windows-based JVMs.
	 *
	 * @return
	 */
	public static SampledProbe<Long> openFileDescriptorCount()
	{
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		if(os instanceof com.sun.management.UnixOperatingSystemMXBean)
		{
			com.sun.management.UnixOperatingSystemMXBean unix =
				(com.sun.management.UnixOperatingSystemMXBean) os;
			return unix::getOpenFileDescriptorCount;
		}

		return () -> -1l;
	}

	/**
	 * Export JVM probes on the given {@link Vibe} instance. In most cases you
	 * want to use a {@link Vibe#scope(String...) scoped instance} with this
	 * method:
	 *
	 * <pre>
	 * // Export all JVM probes under the prefix jvm
	 * JvmProbes.export(Vibe.scope("jvm"));
	 * </pre>
	 *
	 * <p>
	 * This will export:
	 *
	 * <ul>
	 *   <li>{@link #cpuUsage()} at {@code cpu}
	 *   <li>{@link #memoryUsage()} at {@code memory}
	 *   <li>{@link #openFileDescriptorCount()} at {@code openFileDescriptors}
	 *   <li>{@link #threadCount()} at {@code threadCount}
	 *   <li>{@link #loadedClassCount()} at {@code loadedClassCount}
	 *   <li>{@link #directBufferPool()} at {@code buffers/direct}
	 *   <li>{@link #mappedBufferPool() at {@code buffers/mapped}
	 * </ul>
	 *
	 * @param vibe
	 */
	public void export(Vibe vibe)
	{
		vibe.export(cpuUsage())
			.at("cpu")
			.done();

		vibe.export(memoryUsage())
			.at("memory")
			.done();

		vibe.export(openFileDescriptorCount())
			.at("openFileDescriptors")
			.done();

		vibe.export(threadCount())
			.at("threadCount")
			.done();

		vibe.export(loadedClassCount())
			.at("loadedClasses")
			.done();

		vibe.export(directBufferPool())
			.at("buffers", "direct")
			.done();

		vibe.export(mappedBufferPool())
			.at("buffers", "mapped")
			.done();
	}

	/**
	 * Details about heap and non-heap memory usage.
	 *
	 * @see MemoryUsage
	 */
	public static class MemorySnapshot
		implements KeyValueMappable
	{
		private final long heapUsed;
		private final long heapCommitted;
		private final long heapMax;
		private final long nonHeapUsed;
		private final long nonHeapCommitted;
		private final long nonHeapMax;

		public MemorySnapshot(
			long heapUsed,
			long heapCommitted,
			long heapMax,
			long nonHeapUsed,
			long nonHeapCommitted,
			long nonHeapMax
		)
		{
			this.heapUsed = heapUsed;
			this.heapCommitted = heapCommitted;
			this.heapMax = heapMax;
			this.nonHeapUsed = nonHeapUsed;
			this.nonHeapCommitted = nonHeapCommitted;
			this.nonHeapMax = nonHeapMax;
		}

		/**
		 * Get the amount of heap memory used in bytes.
		 *
		 * @return
		 *   heap memory used in bytes
		 */
		public long getHeapUsed()
		{
			return heapUsed;
		}

		/**
		 * Get the amount of heap memory in bytes that is committed for the JVM
		 * to use.
		 *
		 * @return
		 *   committed heap memory in bytes
		 */
		public long getHeapCommitted()
		{
			return heapCommitted;
		}

		/**
		 * Get the maximum amount of heap memory in bytes that can be allocated.
		 * This may return {@code -1} if the maximum is undefined.
		 *
		 * @return
		 *   maximum heap memory in bytes, or {@code -1} if undefined
		 */
		public long getHeapMax()
		{
			return heapMax;
		}

		/**
		 * Get the heap usage as a fraction. Will return {@code -1} if
		 * {@link #getHeapMax()} is undefined.
		 *
		 * @return
		 *   the ratio of used heap memory between {@code 0} and {@code 1},
		 *   or {@code -1} if maximum heap memory is unavailable
		 */
		public double getHeapUsageAsFraction()
		{
			return heapMax < 0 ? -1 : heapUsed / (double) heapMax;
		}

		/**
		 * Get the amount of non-heap memory used in bytes.
		 *
		 * @return
		 *   non-heap memory used in bytes
		 */
		public long getNonHeapUsed()
		{
			return nonHeapUsed;
		}

		/**
		 * Get the amount of non-heap memory in bytes that is committed for the
		 * JVM to use.
		 *
		 * @return
		 *   non-heap memory in bytes that is committed
		 */
		public long getNonHeapCommitted()
		{
			return nonHeapCommitted;
		}

		/**
		 * Get the maximum amount of non-heap memory in bytes that can be
		 * allocated. This may return {@code -1} if the maximum is undefined.
		 *
		 * @return
		 *   maximum non-heap memory in bytes, or {@code -1} if undefined
		 */
		public long getNonHeapMax()
		{
			return nonHeapMax;
		}

		@Override
		public void mapToKeyValues(KeyValueReceiver receiver)
		{
			receiver.add("heapUsed", heapUsed);
			receiver.add("heapCommitted", heapCommitted);
			receiver.add("heapMax", heapMax);
			receiver.add("heapUsageAsFraction", getHeapUsageAsFraction());
			receiver.add("nonHeapUsed", nonHeapUsed);
			receiver.add("nonHeapCommitted", nonHeapCommitted);
			receiver.add("nonHeapMax", nonHeapMax);
		}

		@Override
		public String toString()
		{
			return KeyValueToString.toString(this);
		}
	}

	public static class BufferPoolDetails
		implements KeyValueMappable
	{
		private final long memoryUsed;
		private final long totalCapacity;
		private final long count;

		public BufferPoolDetails(
			long memoryUsed,
			long totalCapacity,
			long count
		)
		{
			this.memoryUsed = memoryUsed;
			this.totalCapacity = totalCapacity;
			this.count = count;
		}

		public long getMemoryUsed()
		{
			return memoryUsed;
		}

		public long getTotalCapacity()
		{
			return totalCapacity;
		}

		public long getCount()
		{
			return count;
		}

		@Override
		public void mapToKeyValues(KeyValueReceiver receiver)
		{
			receiver.add("memoryUsed", memoryUsed);
			receiver.add("totalCapacity", totalCapacity);
			receiver.add("count", count);
		}

		@Override
		public String toString()
		{
			return KeyValueToString.toString(this);
		}
	}
}
