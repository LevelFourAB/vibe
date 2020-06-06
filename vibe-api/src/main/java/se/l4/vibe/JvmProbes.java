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
	 * Get a probe for measuring the CPU usage of this JVM process as factor
	 * between 0 to 1.
	 *
	 * @return
	 */
	public static SampledProbe<Double> cpuUsage()
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

				if(lastUptime == 0)
				{
					lastUptime = uptime;
					lastCpu = cpu;
					return Double.NaN;
				}

				long elapsedCpu = cpu - lastCpu;
				long elapsedTime = uptime - lastUptime;

				double v = Math.min(99, elapsedCpu / (elapsedTime * 10000.0 * rt.availableProcessors())) / 100;

				lastUptime = uptime;
				lastCpu = cpu;

				return v;
			}
		};
	}

	public static Probe<MemoryDetails> memoryUsage()
	{
		final MemoryMXBean b = ManagementFactory.getMemoryMXBean();

		return () -> {
			MemoryUsage heap = b.getHeapMemoryUsage();
			MemoryUsage nonHeap = b.getNonHeapMemoryUsage();

			return new MemoryDetails(
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
	 * Get the number of open file descriptors.
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

	public static class MemoryDetails
		implements KeyValueMappable
	{
		private final long heapUsed;
		private final long heapCommitted;
		private final long heapMax;
		private final long nonHeapUsed;
		private final long nonHeapCommitted;
		private final long nonHeapMax;

		public MemoryDetails(long heapUsed, long heapCommitted, long heapMax,
				long nonHeapUsed, long nonHeapCommitted, long nonHeapMax)
		{
			this.heapUsed = heapUsed;
			this.heapCommitted = heapCommitted;
			this.heapMax = heapMax;
			this.nonHeapUsed = nonHeapUsed;
			this.nonHeapCommitted = nonHeapCommitted;
			this.nonHeapMax = nonHeapMax;
		}

		public long getHeapUsed()
		{
			return heapUsed;
		}

		public long getHeapCommitted()
		{
			return heapCommitted;
		}

		public long getHeapMax()
		{
			return heapMax;
		}

		public long getNonHeapUsed()
		{
			return nonHeapUsed;
		}

		public long getNonHeapCommitted()
		{
			return nonHeapCommitted;
		}

		public long getNonHeapMax()
		{
			return nonHeapMax;
		}

		public double getHeapUsageAsFraction()
		{
			return heapUsed / (double) heapMax;
		}

		@Override
		public void mapToKeyValues(KeyValueReceiver receiver)
		{
			receiver.add("heapUsed", heapUsed);
			receiver.add("heapCommitted", heapCommitted);
			receiver.add("heapMax", heapMax);
			receiver.add("nonHeapUsed", nonHeapUsed);
			receiver.add("nonHeapCommitted", nonHeapCommitted);
			receiver.add("nonHeapMax", nonHeapMax);
			receiver.add("heapUsageAsFraction", getHeapUsageAsFraction());
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

		public BufferPoolDetails(long memoryUsed, long totalCapacity, long count)
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
