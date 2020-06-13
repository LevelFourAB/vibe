package se.l4.vibe.timers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TimerTest
{
	@Test
	public void testNanoResolution()
		throws Exception
	{
		Timer timer = Timer.builder()
			.withResolution(TimeUnit.NANOSECONDS)
			.build();

		try(Stopwatch w = timer.start())
		{
			Thread.sleep(1);
		}

		assertThat(timer.getMaximumProbe().read(), is(greaterThan(100000l)));
	}

	@Test
	public void testMilliResolution()
		throws Exception
	{
		Timer timer = Timer.builder()
			.withResolution(TimeUnit.MILLISECONDS)
			.build();

		try(Stopwatch w = timer.start())
		{
			Thread.sleep(2);
		}

		assertThat(timer.getMaximumProbe().read(), is(greaterThan(1l)));
	}
}
