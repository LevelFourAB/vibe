package se.l4.vibe.sampling;

import java.util.Collection;

import se.l4.vibe.operations.TimeLimited;

/**
 * Operation on list of {@link Sample samples}. This type of operation has
 * access to all samples within a limited span, either based on the number
 * of samples or a time period. It can be used to calculate things like
 * averages, minimum and maximum values for these samples.
 *
 * <p>
 * {@link TimeLimited} can be used to create {@link SampleOperation}s that
 * use this interface to calculate something within a certain time period.
 */
public interface SampleListOperation<Input, Output>
{
	/**
	 * Handle a sample being removed from the list.
	 *
	 * @param sample
	 * 	 the sample being removed
	 * @param samples
	 *   all of the samples, without the sample being removed
	 */
	void remove(Sample<Input> sample, Collection<Sample<Input>> samples);

	/**
	 * Handle a sample being added to the list.
	 *
	 * @param sample
	 *   the sample being added
	 * @param samples
	 *   all of the samples, including the value added
	 */
	void add(Sample<Input> sample, Collection<Sample<Input>> samples);

	/**
	 * Get the value of this
	 *
	 * @return
	 */
	Output get();
}
