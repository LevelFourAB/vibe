package se.l4.vibe.backend;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import se.l4.vibe.event.Events;
import se.l4.vibe.internal.jmx.ProbeBean;
import se.l4.vibe.internal.jmx.SamplerBean;
import se.l4.vibe.internal.jmx.ServiceMBeanBridge;
import se.l4.vibe.internal.service.Service;
import se.l4.vibe.internal.service.ServiceImpl;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.Sampler;
import se.l4.vibe.timer.Timer;

/**
 * JMX backend that will export everything via JMX.
 *
 * @author Andreas Holstenson
 *
 */
public class JmxBackend
	implements VibeBackend
{
	private final MBeanServer server;
	private final String root;

	public JmxBackend()
	{
		this("vibe");
	}

	public JmxBackend(String root)
	{
		this(root, ManagementFactory.getPlatformMBeanServer());
	}

	public JmxBackend(MBeanServer server)
	{
		this("vibe", server);
	}

	public JmxBackend(String root, MBeanServer server)
	{
		// Cut of last . if any
		this.root = root.charAt(root.length() - 1) == '.'
			? root.substring(0, root.length() - 1)
			: root;
		this.server = server;
	}

	/**
	 * Translate a path into a JMX location.
	 *
	 * @param path
	 * @return
	 */
	private String toJmxLocation(String path)
	{
		StringBuilder builder = new StringBuilder(path.length());
		int lastSeparator = path.lastIndexOf('/');

		if(! "".equals(root))
		{
			// Append the root domain
			builder
				.append(root)
				.append(".");
		}

		if(lastSeparator == -1)
		{
			// No separator, append name directly
			builder.append(":name=");
		}

		for(int i=0, n=path.length(); i<n; i++)
		{
			char c = path.charAt(i);

			if(i < lastSeparator)
			{
				if(c == '.')
				{
					builder.append(' ');
				}
				else if(c == '/')
				{
					builder.append('.');
				}
				else
				{
					builder.append(c);
				}
			}
			else if(i == lastSeparator)
			{
				builder.append(":name=");
			}
			else
			{
				if(c == '.')
				{
					builder.append(' ');
				}
				else
				{
					builder.append(c);
				}
			}
		}

		return builder.toString();
	}

	private Handle export(String path, Service service)
	{
		String jmxLocation = toJmxLocation(path);
		try
		{
			server.registerMBean(
				new ServiceMBeanBridge(jmxLocation, service),
				new ObjectName(jmxLocation)
			);

			return () -> {
				try
				{
					server.unregisterMBean(new ObjectName(jmxLocation));
				}
				catch(InstanceNotFoundException | MBeanRegistrationException | MalformedObjectNameException e)
				{
					// The object is probably no longer registered so ignore this
				}
			};
		}
		catch(InstanceAlreadyExistsException e)
		{
			throw new RuntimeException("Something has already been registered at " + path + " (JMX location: " + jmxLocation + ")");
		}
		catch(MBeanRegistrationException | NotCompliantMBeanException | MalformedObjectNameException e)
		{
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public Handle export(String path, Object object)
	{
		return export(path, new ServiceImpl(object));
	}

	@Override
	public Handle export(String path, Sampler<?> series)
	{
		return export(path, new SamplerBean(series));
	}

	@Override
	public Handle export(String path, Probe<?> probe)
	{
		return export(path, new ProbeBean(probe));
	}

	@Override
	public Handle export(String path, Events<?> events)
	{
		return Handle.empty();
	}

	@Override
	public Handle export(String path, Timer timer)
	{
		return Handle.empty();
	}

	@Override
	public void close()
	{
	}
}
