package se.l4.vibe.internal.sampling;

import java.util.LinkedList;

import se.l4.vibe.operations.Operation;
import se.l4.vibe.operations.OperationExecutor;
import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.SampleListOperation;

/**
 * {@link Operation} that uses a rolling time window and
 * {@link SampleListOperation} to calculate its value.
 *
 * @param <Input>
 * @param <Output>
 */
public class RollingTimeLimitedSampleOperation<Input, Output>
	implements OperationExecutor<Sample<Input>, Sample<Output>>
{
	private final long maxAge;
	private final SampleListOperation<Input, Output> operationToApply;

	private final LinkedList<Sample<Input>> samples;

	public RollingTimeLimitedSampleOperation(
		long maxAge,
		SampleListOperation<Input, Output> operationToApply
	)
	{
		this.maxAge = maxAge;
		this.operationToApply = operationToApply;

		samples = new LinkedList<>();
	}

	@Override
	public Sample<Output> apply(Sample<Input> sample)
	{
		long cutOff = System.currentTimeMillis() - maxAge;
		while(! samples.isEmpty())
		{
			/*
			 * If we have entries check if the first one should be
			 * removed or kept.
			 */
			Sample<Input> firstSample = samples.getFirst();
			if(firstSample.getTime() < cutOff)
			{
				samples.removeFirst();

				operationToApply.remove(firstSample, samples);
			}
			else
			{
				// This sample is within the acceptable age, continue with adding the new one
				break;
			}
		}

		samples.add(sample);
		operationToApply.add(sample, samples);

		return Sample.create(sample.getTime(), operationToApply.get());
	}
}
