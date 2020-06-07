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

/**
 * Wrapper that will turn a service into a MBean.
 *
 */
public class ExportMBeanBridge
	implements DynamicMBean
{
	private final JmxExport object;
	private final MBeanInfo info;

	public ExportMBeanBridge(
		String location,
		JmxExport object
	)
	{
		this.object = object;

		List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();
		for(JmxExport.Attribute attr : object.getAttributes())
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
		return object.getAttribute(attribute);
	}

	@Override
	public void setAttribute(Attribute attribute)
		throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
	{
	}

	@Override
	public AttributeList getAttributes(String[] attributes)
	{
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
		return null;
	}

	@Override
	public MBeanInfo getMBeanInfo()
	{
		return info;
	}

}
