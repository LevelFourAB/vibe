package se.l4.vibe.internal.builder;

import se.l4.vibe.builder.Builder;

/**
 * Abstract implementation of a builder.
 *
 * @author Andreas Holstenson
 *
 * @param <Self>
 */
public class AbstractBuilder<Self>
	implements Builder<Self>
{
	protected String path;

	@Override
	public Self at(String path)
	{
		this.path = path;

		return (Self) this;
	}

	@Override
	public Self at(String... hierarchy)
	{
		StringBuilder path = new StringBuilder();
		for(int i=0, n=hierarchy.length; i<n; i++)
		{
			if(i > 0) path.append('/');

			String segment = hierarchy[i];
			if(segment.indexOf('/') != -1)
			{
				throw new IllegalArgumentException("Segments may not contain /; For " + segment);
			}

			path.append(segment);
		}

		this.path = path.toString();

		return (Self) this;
	}

	@Override
	public Self at(Class<?> type)
	{
		if(type == null)
		{
			throw new IllegalArgumentException("Type can not be null");
		}

		this.path = type.getName().replace('.', '/');

		return (Self) this;
	}

	protected void verify()
	{
		if(path == null)
		{
			throw new IllegalStateException("A path is required");
		}
	}
}
