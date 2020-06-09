package se.l4.vibe.influxdb.internal;

import java.util.Map;

/**
 * Data point to be sent to InfluxDB.
 */
public class DataPoint
{
	private final String measurement;
	private final long time;
	private final Map<String, String> tags;
	private final Map<String, Object> values;

	public DataPoint(String measurement, long time, Map<String, String> tags, Map<String, Object> values)
	{
		this.measurement = measurement;
		this.time = time;
		this.tags = tags;
		this.values = values;
	}

	public String toLine()
	{
		StringBuilder builder = new StringBuilder();
		escapeInto(measurement, builder);

		for(Map.Entry<String, String> tag : tags.entrySet())
		{
			builder.append(',');
			escapeInto(tag.getKey(), builder);
			builder.append('=');
			escapeInto(tag.getValue(), builder);
		}
		builder.append(' ');

		boolean first = true;
		for(Map.Entry<String, Object> value : values.entrySet())
		{
			if(first)
			{
				first = false;
			}
			else
			{
				builder.append(',');
			}
			escapeInto(value.getKey(), builder);
			builder.append('=');
			appendValueTo(value.getValue(), builder);
		}
		builder.append(' ');
		builder.append(time);

		return builder.toString();
	}

	private void escapeInto(String value, StringBuilder builder)
	{
		for(int i=0, n=value.length(); i<n; i++)
		{
			char c = value.charAt(i);
			if(c == ' ' || c == ',' || c == '=')
			{
				builder.append('\\');
			}
			builder.append(c);
		}
	}

	private void appendValueTo(Object v, StringBuilder builder)
	{
		if(v instanceof Boolean)
		{
			builder.append(((Boolean) v).booleanValue() == true ? 't' : 'f');
		}
		else if(v instanceof Long || v instanceof Integer)
		{
			builder.append(v.toString()).append('i');
		}
		else if(v instanceof Float || v instanceof Double)
		{
			builder.append(v.toString());
		}
		else
		{
			// Treat everything else as a string
			builder.append('"');
			String value = v.toString();
			for(int i=0, n=value.length(); i<n; i++)
			{
				char c = value.charAt(i);
				if(c == '"')
				{
					builder.append('\\');
				}
				builder.append(c);
			}
			builder.append('"');
		}
	}
}
