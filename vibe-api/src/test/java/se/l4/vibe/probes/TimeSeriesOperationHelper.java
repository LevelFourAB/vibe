package se.l4.vibe.probes;

import java.util.LinkedList;

import se.l4.vibe.probes.Sampler.Entry;

/**
 * Utility class to help with testing of {@link SampleOperation}s.
 *
 * @author Andreas Holstenson
 *
 * @param <Input>
 * @param <Output>
 */
public class TimeSeriesOperationHelper<Input, Output>
{
	private LinkedList<Sampler.Entry<Input>> list;
	private final SampleOperation<Input, Output> op;

	public TimeSeriesOperationHelper(SampleOperation<Input, Output> op)
	{
		this.op = op;
		list = new LinkedList<Sampler.Entry<Input>>();
	}

	public static <Input, Output> TimeSeriesOperationHelper<Input, Output>
		create(SampleOperation<Input, Output> op)
	{
		return new TimeSeriesOperationHelper<Input, Output>(op);
	}

	public void add(final Input value)
	{
		list.add(new Entry<Input>()
		{
			@Override
			public long getTime()
			{
				return 0;
			}

			@Override
			public Input getValue()
			{
				return value;
			}

			@Override
			public String toString()
			{
				return "Entry{time=0, value=" + value + "}";
			}
		});
		op.add(value, list);
	}

	public void removeFirst()
	{
		Entry<Input> entry = list.removeFirst();
		op.remove(entry.getValue(), list);
	}

	public Output get()
	{
		return op.get();
	}
}
