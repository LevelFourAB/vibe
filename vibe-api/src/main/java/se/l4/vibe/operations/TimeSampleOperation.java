package se.l4.vibe.operations;

import se.l4.vibe.sampling.Sample;

/**
 * Named extension to {@link Operation} for use with {@link Sample}s.
 *
 * @param <Input>
 * @param <Output>
 */
@FunctionalInterface
public interface TimeSampleOperation<Input, Output>
	extends Operation<Sample<Input>, Sample<Output>>
{
	/**
	 * Create an operation that turns any {@link Operation} into one that takes
	 * and outputs {@link Sample}.
	 *
	 * @param <Input>
	 * @param <Output>
	 * @param op
	 * @return
	 */
	static <Input, Output> TimeSampleOperation<Input, Output> over(Operation<Input, Output> op)
	{
		return sample -> Sample.create(sample.getTime(), op.apply(sample.getValue()));
	}
}
