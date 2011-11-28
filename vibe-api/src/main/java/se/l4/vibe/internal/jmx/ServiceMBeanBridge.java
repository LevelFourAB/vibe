package se.l4.vibe.internal.jmx;

import java.util.ArrayList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import se.l4.vibe.service.Service;

/**
 * Wrapper that will turn a service into a MBean.
 * 
 * @author Andreas Holstenson
 *
 */
public class ServiceMBeanBridge
	implements DynamicMBean
{
	private final Service service;
	private final MBeanInfo info;

	public ServiceMBeanBridge(String location, Service service)
	{
		this.service = service;
		
		List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();
		for(Service.Attribute attr : service.getAttributes())
		{
			attributes.add(new MBeanAttributeInfo(attr.getName(), "", toType(attr.getType()), true, false, false));
		}
		
		info = new MBeanInfo(
			location, "", 
			attributes.toArray(new MBeanAttributeInfo[0]), 
			new MBeanConstructorInfo[0],
			new MBeanOperationInfo[0], 
			new MBeanNotificationInfo[0]
		);
	}
	
	private String toType(Class<?> type)
	{
		if(type.isPrimitive())
		{
			return type.getName();
		}
		
		// TODO: Better mapping?
		return String.class.getName();
	}
	
	@Override
	public Object getAttribute(String attribute)
		throws AttributeNotFoundException, MBeanException, ReflectionException
	{
		Service.Attribute attr = service.getAttribute(attribute);
		if(attr == null) throw new AttributeNotFoundException("Unable to find " + attribute);
		
		return attr.getValue();
	}

	@Override
	public void setAttribute(Attribute attribute)
		throws AttributeNotFoundException, InvalidAttributeValueException,
		MBeanException, ReflectionException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public AttributeList getAttributes(String[] attributes)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes)
	{
		
		return null;
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature)
		throws MBeanException, ReflectionException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MBeanInfo getMBeanInfo()
	{
		return info;
	}

}
