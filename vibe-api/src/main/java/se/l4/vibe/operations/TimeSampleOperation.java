package se.l4.vibe.operations;

import edu.umd.cs.findbugs.annotations.NonNull;
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
	@NonNull
	static <Input, Output> TimeSampleOperation<Input, Output> over(
		@NonNull Operation<Input, Output> op
	)
	{
		return () -> {
			OperationExecutor<Input, Output> executor = op.create();
			return sample -> Sample.create(
				sample.getTime(),
				executor.apply(sample.getValue())
			);
		};
	}
}
