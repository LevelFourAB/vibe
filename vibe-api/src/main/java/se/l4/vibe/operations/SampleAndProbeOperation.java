package se.l4.vibe.operations;

import se.l4.vibe.probes.ProbeOperation;
import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.SampleOperation;

/**
 * Combination of {@link ProbeOperation} and {@link SampleOperation}.
 *
 * @param <Input>
 * @param <Output>
 */
public interface SampleAndProbeOperation<Input, Output>
	extends ProbeOperation<Input, Output>, SampleOperation<Input, Output>
{
	@Override
	default Sample<Output> handleSample(Sample<Input> sample)
	{
		return Sample.create(sample.getTime(), apply(sample.getValue()));
	}
}
