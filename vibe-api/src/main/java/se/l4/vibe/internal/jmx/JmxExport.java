package se.l4.vibe.internal.jmx;

import java.util.Collection;

/**
 * Something being exposed over JMX.
 */
public interface JmxExport
{
	/**
	 * Get exposed attributes.
	 *
	 * @return
	 */
	Collection<Attribute> getAttributes();

	/**
	 * Get a specific attribute from the service.
	 *
	 * @param attribute
	 * @return
	 */
	Object getAttribute(String attribute);

	/**
	 * Attribute information for service.
	 */
	class Attribute
	{
		private final String name;
		private final Class<?> type;

		public Attribute(String name, Class<?> type)
		{
			this.name = name;
			this.type = type;
		}

		public String getName()
		{
			return name;
		}

		public Class<?> getType()
		{
			return type;
		}
	}
}
