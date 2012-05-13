package se.l4.vibe.internal.service;

import java.util.Collection;

/**
 * Information about a service that has been exposed.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Service
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
	Attribute getAttribute(String attribute);

	/**
	 * Attribute information for service.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	interface Attribute
	{
		/**
		 * Get the name of the attribute.
		 * 
		 * @return
		 */
		String getName();
		
		/**
		 * Get the type of the attribute.
		 * 
		 * @return
		 */
		Class<?> getType();
		
		/**
		 * Get the value of the attribute.
		 * 
		 * @return
		 */
		Object getValue();
	}
}
