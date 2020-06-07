package se.l4.vibe.backend;

import java.lang.management.ManagementFactory;
import java.util.Objects;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import se.l4.vibe.ListenerHandle;
import se.l4.vibe.internal.jmx.ExportMBeanBridge;
import se.l4.vibe.internal.jmx.JmxExport;
import se.l4.vibe.internal.jmx.ProbeBean;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.Sampler;

/**
 * Backend that will probes and samplers over JMX.
 */
public class JmxBackend
	implements VibeBackend
{
	private final MBeanServer server;
	private final String root;

	private JmxBackend(String root, MBeanServer server)
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

	private Handle export0(String path, JmxExport object)
	{
		String jmxLocation = toJmxLocation(path);
		try
		{
			server.registerMBean(
				new ExportMBeanBridge(jmxLocation, object),
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

	@Override
	public Handle export(String path, Sampler<?> series)
	{
		/*
		 * Add a listener - doesn't actually do anything other than to activate
		 * sampling.
		 */
		ListenerHandle h1 = series.addListener(sample -> {});
		Handle h2 = export0(path, new ProbeBean(series));

		return () -> {
			h1.remove();
			h2.remove();
		};
	}

	@Override
	public Handle export(String path, Probe<?> probe)
	{
		return export0(path, new ProbeBean(probe));
	}

	@Override
	public void close()
	{
	}

	/**
	 * Start building a new JMX backend.
	 *
	 * @return
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	public static class Builder
	{
		private String name;
		private MBeanServer server;

		public Builder()
		{
			name = "vibe";
			server = ManagementFactory.getPlatformMBeanServer();
		}

		public Builder setName(String name)
		{
			Objects.requireNonNull(name, "name can not be null");

			this.name = name;
			return this;
		}

		public Builder setServer(MBeanServer server)
		{
			Objects.requireNonNull(server, "server can not be null");

			this.server = server;
			return this;
		}

		public JmxBackend build()
		{
			return new JmxBackend(name, server);
		}
	}
}
