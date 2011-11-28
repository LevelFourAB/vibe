package se.l4.vibe.probes;

/**
 * Samples measured over time for a certain {@link SampledProbe}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TimeSeries<T>
	extends Iterable<TimeSeries.Entry<T>>
{
	/**
	 * Entry within a {@link TimeSeries}.
	 * 
	 * @author Andreas Holstenson
	 *
	 * @param <T>
	 */
	interface Entry<T>
	{
		/**
		 * Get the time this was sampled.
		 * 
		 * @return
		 */
		long getTime();
		
		/**
		 * Get the value of the sample.
		 * 
		 * @return
		 */
		T getValue();
	}
	
	/**
	 * Get the probe that was used.
	 * 
	 * @return
	 */
	SampledProbe<T> getProbe();
	
	/**
	 * Add a listener to this series.
	 * 
	 * @param listener
	 */
	void addListener(SampleListener<T> listener);
	
	/**
	 * Remove a listener from this series.
	 * 
	 * @param listener
	 */
	void removeListener(SampleListener<T> listener);
}
