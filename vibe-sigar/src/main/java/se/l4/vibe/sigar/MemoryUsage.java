package se.l4.vibe.sigar;

import org.hyperic.sigar.Mem;

/**
 * Memory usage information, copy of data contained in {@link Mem}.
 * 
 * @author Andreas Holstenson
 *
 */
public class MemoryUsage
{
	private final long actualFree;
	private final long actualUsed;
	
	
	private final long ram;
	private final long total;
	private final long free;
	private final long used;
	
	private final double freePercent;
	private final double usedPercent;

	public MemoryUsage()
	{
		actualFree = -1;
		actualUsed = -1;
		free = -1;
		freePercent = -1;
		ram = -1;
		total = -1;
		used = -1;
		usedPercent = -1;
	}
	
	public MemoryUsage(Mem mem)
	{
		actualFree = mem.getActualFree();
		actualUsed = mem.getActualUsed();
		free = mem.getFree();
		freePercent = mem.getFreePercent();
		ram = mem.getRam();
		total = mem.getTotal();
		used = mem.getUsed();
		usedPercent = mem.getUsedPercent();
	}
	
	public long getActualFree()
	{
		return actualFree;
	}
	
	public long getActualUsed()
	{
		return actualUsed;
	}
	
	public long getRam()
	{
		return ram;
	}

	public long getTotal()
	{
		return total;
	}
	
	public long getFree()
	{
		return free;
	}
	
	public long getUsed()
	{
		return used;
	}
	
	public double getFreePercent()
	{
		return freePercent;
	}
	
	public double getUsedPercent()
	{
		return usedPercent;
	}
}
