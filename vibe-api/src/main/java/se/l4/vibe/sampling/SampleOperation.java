package se.l4.vibe.sampling;

/**
 * Operation that can be applied to samples of a {@link TimeSampler}. Used to
 * transform and aggregate samples.
 *
 * @param <Input>
 * @param <Output>
 */
public interface SampleOperation<Input, Output>
{
	/**
	 * Handle the given sample returning a transformed sample. This method will
	 * be called for every sample and must always return a new sample.
	 *
	 * <p>
	 * The returned sample may change the timestamp, for example for a 5 minute
	 * average an operation may opt to implement a bucket approach where the
	 * sample returned is for the last whole period of 5 minutes instead of
	 * a rolling average.
	 *
	 * @param sample
	 * @return
	 */
	Sample<Output> handleSample(Sample<Input> sample);
}
