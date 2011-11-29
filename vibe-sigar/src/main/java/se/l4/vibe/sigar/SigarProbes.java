package se.l4.vibe.sigar;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import se.l4.vibe.VibeException;
import se.l4.vibe.probes.AbstractSampledProbe;
import se.l4.vibe.probes.SampledProbe;

/**
 * Probes for that use Sigar for monitoring the system. Please note that the
 * native libraries need to be available for this to work. You should check
 * availability with {@link #isAvailable()} before using these probes.
 * 
 * @author Andreas Holstenson
 *
 */
public class SigarProbes
{
	private final Sigar sigar;
	
	public SigarProbes()
	{
		Sigar sigar = new Sigar();
		try
		{
			sigar.getPid();
		}
		catch(Throwable t)
		{
			try
			{
				sigar.close();
			}
			catch(Throwable t2)
			{
			}
			
			sigar = null;
		}
		
		this.sigar = sigar;
	}
	
	/**
	 * Check if probes are available.
	 * 
	 * @return
	 */
	public boolean isAvailable()
	{
		return sigar != null;
	}
	
	private void verify()
	{
		if(! isAvailable())
		{
			throw new VibeException("Sigar is not avaiable");
		}
	}
	
	/**
	 * Get a probe for CPU usage as a fraction. Please note that each instance 
	 * of {@link SigarProbes} can only have one accurate CPU probe. Calling
	 * this method more than once will cause your measurements to be off.
	 * 
	 * @return
	 */
	public SampledProbe<Double> getCpuUsage()
	{
		verify();
		
		try
		{
			sigar.getCpuPerc();
		}
		catch(SigarException e1)
		{
		}
		
		return new AbstractSampledProbe<Double>()
		{
			@Override
			public Double peek()
			{
				return value;
			}
			
			@Override
			protected Double sample0()
			{
				try
				{
					CpuPerc perc = sigar.getCpuPerc();
					return perc.getCombined();
				}
				catch(SigarException e)
				{
					return Double.NaN;
				}
			}
		};
	}
	
	/**
	 * Get memory information.
	 * 
	 * @return
	 */
	public SampledProbe<MemoryUsage> getMemoryUsage()
	{
		verify();
		
		return new AbstractSampledProbe<MemoryUsage>()
		{
			@Override
			public MemoryUsage peek()
			{
				return sample0();
			}
			
			@Override
			protected MemoryUsage sample0()
			{
				try
				{
					Mem mem = sigar.getMem();
					return new MemoryUsage(mem);
				}
				catch(SigarException e)
				{
					return new MemoryUsage();
				}
			}
		};
	}
	
	/**
	 * Get used memory as a fraction. Uses {@link Mem#getUsedPercent()}.
	 * 
	 * @return
	 */
	public SampledProbe<Double> getUsedMemoryAsFraction()
	{
		verify();
		
		return new AbstractSampledProbe<Double>()
		{
			@Override
			public Double peek()
			{
				return sample0();
			}
			
			@Override
			protected Double sample0()
			{
				try
				{
					Mem mem = sigar.getMem();
					return mem.getUsedPercent() / 100;
				}
				catch(SigarException e)
				{
					return Double.NaN;
				}
			}
		};
	}
	
	/**
	 * Get the amount of used memory in bytes. Uses {@link Mem#getUsed()}.
	 * 
	 * @return
	 */
	public SampledProbe<Long> getUsedMemoryInBytes()
	{
		verify();
		
		return new AbstractSampledProbe<Long>()
		{
			@Override
			public Long peek()
			{
				return sample0();
			}
			
			@Override
			protected Long sample0()
			{
				try
				{
					Mem mem = sigar.getMem();
					return mem.getUsed();
				}
				catch(SigarException e)
				{
					return -1l;
				}
			}
		};
	}
	
	/**
	 * Get free memory as a fraction. Uses {@link Mem#getFreePercent()}.
	 * 
	 * @return
	 */
	public SampledProbe<Double> getFreeMemoryAsFraction()
	{
		verify();
		
		return new AbstractSampledProbe<Double>()
		{
			@Override
			public Double peek()
			{
				return sample0();
			}
			
			@Override
			protected Double sample0()
			{
				try
				{
					Mem mem = sigar.getMem();
					return mem.getFreePercent() / 100;
				}
				catch(SigarException e)
				{
					return Double.NaN;
				}
			}
		};
	}
	
	/**
	 * Get the amount of free memory in bytes. Uses {@link Mem#getFree()}.
	 * 
	 * @return
	 */
	public SampledProbe<Long> getFreeMemoryInBytes()
	{
		verify();
		
		return new AbstractSampledProbe<Long>()
		{
			@Override
			public Long peek()
			{
				return sample0();
			}
			
			@Override
			protected Long sample0()
			{
				try
				{
					Mem mem = sigar.getMem();
					return mem.getFree();
				}
				catch(SigarException e)
				{
					return -1l;
				}
			}
		};
	}
}
