package se.l4.vibe.internal.builder;

import se.l4.vibe.backend.VibeBackend;
import se.l4.vibe.builder.TimerBuilder;
import se.l4.vibe.internal.timer.TimerImpl;
import se.l4.vibe.percentile.BucketPercentileCounter;
import se.l4.vibe.percentile.FakePercentileCounter;
import se.l4.vibe.percentile.PercentileCounter;
import se.l4.vibe.timer.Timer;

/**
 * Implementation of {@link TimerBuilder}.
 *
 * @author Andreas Holstenson
 *
 */
public class TimerBuilderImpl
	extends AbstractBuilder<TimerBuilder>
	implements TimerBuilder
{
	private VibeBackend backend;
	private PercentileCounter percentileCounter;

	public TimerBuilderImpl(VibeBackend backend)
	{
		this.backend = backend;
		percentileCounter = new FakePercentileCounter();
	}

	@Override
	public TimerBuilder withBuckets(int... limits)
	{
		int[] msLimits = new int[limits.length];
		for(int i=0, n=limits.length; i<n; i++)
		{
			msLimits[i] = limits[i] * 1000000;
		}
		return withPercentiles(new BucketPercentileCounter(msLimits));
	}

	@Override
	public TimerBuilder withPercentiles(PercentileCounter counter)
	{
		this.percentileCounter = counter;
		return this;
	}

	@Override
	public Timer build()
	{
		return new TimerImpl(percentileCounter);
	}

	@Override
	public Timer export()
	{
		verify();

		Timer result = build();

		backend.export(path, result);

		return result;
	}
}
