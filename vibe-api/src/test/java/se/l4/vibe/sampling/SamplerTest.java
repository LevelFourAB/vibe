package se.l4.vibe.sampling;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

public class SamplerTest
{
	@Test
	public void testAddingListenerStartsSampling()
	{
		TimeSampler<Double> randomSampler = TimeSampler.forProbe(
			() -> ThreadLocalRandom.current().nextDouble()
		).build();

		assertThat(randomSampler, is(notNullValue()));

		randomSampler.addListener(sample -> {});

		assertThat(randomSampler.getLastSample(), is(notNullValue()));
	}

	@Test
	public void testSamplingReturnsValue()
	{
		TimeSampler<Integer> randomSampler = TimeSampler.forProbe(() -> 10)
			.build();

		assertThat(randomSampler, is(notNullValue()));

		randomSampler.addListener(sample -> {});

		assertThat(randomSampler.getLastSample().getValue(), is(10));
	}

	@Test
	public void testSamplingWithApplyWorks()
	{
		TimeSampler<Integer> randomSampler = TimeSampler.forProbe(() -> 10)
			.apply(i -> i * 10)
			.build();

		assertThat(randomSampler, is(notNullValue()));

		randomSampler.addListener(sample -> {});

		assertThat(randomSampler.getLastSample().getValue(), is(100));
	}
}
