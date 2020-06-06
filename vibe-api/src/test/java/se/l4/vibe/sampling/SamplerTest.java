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
		Sampler<Double> randomSampler = Sampler.forProbe(
			() -> ThreadLocalRandom.current().nextDouble()
		).build();

		assertThat(randomSampler, is(notNullValue()));

		randomSampler.addListener(sample -> {});

		assertThat(randomSampler.read(), is(notNullValue()));
	}
}
