package se.l4.vibe.internal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import se.l4.vibe.service.ExposeAsAttribute;
import se.l4.vibe.service.Service;

/**
 * Implementation of {@link Service} that uses reflection.
 * 
 * @author Andreas Holstenson
 *
 */
public class ServiceImpl
	implements Service
{
	private final Object object;
	private final ConcurrentMap<String, Attribute> attributes;

	public ServiceImpl(Object object)
	{
		this.object = object;
		
		attributes = new ConcurrentHashMap<String, Attribute>();
		buildAttributes(attributes, object.getClass());
	}
	
	private void buildAttributes(ConcurrentMap<String, Attribute> attributes, Class<?> type)
	{
		while(type != Object.class)
		{
			for(Field field : type.getDeclaredFields())
			{
				if(field.isAnnotationPresent(ExposeAsAttribute.class))
				{
					Attribute attr = new AttributeViaField(field);
					if(attributes.containsKey(attr.getName()))
					{
						throw new IllegalArgumentException("Attribute named " 
							+ attr.getName() + " already exists; Declared via " 
							+ attributes.get(attr.getName()));
					}
					
					attributes.put(attr.getName(), attr);
				}
			}
			
			for(Method method : type.getDeclaredMethods())
			{
				if(method.isAnnotationPresent(ExposeAsAttribute.class))
				{
					Attribute attr = new AttributeViaMethod(method);
					if(attributes.containsKey(attr.getName()))
					{
						throw new IllegalArgumentException("Attribute named " 
							+ attr.getName() + " already exists; Declared via " 
							+ attributes.get(attr.getName()));
					}
					
					attributes.put(attr.getName(), attr);
				}
			}
			
			type = type.getSuperclass();
		}
	}
	
	@Override
	public Collection<Attribute> getAttributes()
	{
		return attributes.values();
	}
	
	@Override
	public Attribute getAttribute(String attribute)
	{
		return attributes.get(attribute);
	}
	
	private class AttributeViaField
		implements Attribute
	{
		private final Field field;
		private final String name;

		public AttributeViaField(Field field)
		{
			this.field = field;
			field.setAccessible(true);
			
			ExposeAsAttribute annotation = field.getAnnotation(ExposeAsAttribute.class);
			this.name = annotation.value().equals("") ? field.getName() : annotation.value();
		}
		
		@Override
		public String getName()
		{
			return name;
		}
		
		@Override
		public Class<?> getType()
		{
			return field.getType();
		}
		
		@Override
		public Object getValue()
		{
			try
			{
				return field.get(object);
			}
			catch(IllegalArgumentException e)
			{
				throw new RuntimeException("Unable to get value of field " + field + "; " + e.getMessage(), e);
			}
			catch(IllegalAccessException e)
			{
				throw new RuntimeException("Unable to get value of field " + field + "; " + e.getMessage(), e);
			}
		}
		
		@Override
		public String toString()
		{
			return "AttributeViaField[" + field + "]";
		}
	}
	
	private class AttributeViaMethod
		implements Attribute
	{
		private final Method method;
		private final String name;

		public AttributeViaMethod(Method method)
		{
			this.method = method;
			method.setAccessible(true);
			
			if(method.getParameterTypes().length != 0)
			{
				throw new IllegalArgumentException("Method " + method + " must not have any parameters");
			}
			
			if(method.getReturnType() == void.class)
			{
				throw new IllegalArgumentException("Method " + method + " must not have a non void return type");
			}
			
			ExposeAsAttribute annotation = method.getAnnotation(ExposeAsAttribute.class);
			this.name = annotation.value().equals("") ? method.getName() : annotation.value();
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public Class<?> getType()
		{
			return method.getReturnType();
		}

		@Override
		public Object getValue()
		{
			try
			{
				return method.invoke(object);
			}
			catch(IllegalArgumentException e)
			{
				throw new RuntimeException("Unable to get value; " + e.getMessage(), e);
			}
			catch(IllegalAccessException e)
			{
				throw new RuntimeException("Unable to get value; " + e.getMessage(), e);
			}
			catch(InvocationTargetException e)
			{
				throw new RuntimeException("Unable to get value; " + e.getCause().getMessage(), e.getCause());
			}
		}
		
		@Override
		public String toString()
		{
			return "AttributeViaMethod[" + method + "]";
		}
	}
}
