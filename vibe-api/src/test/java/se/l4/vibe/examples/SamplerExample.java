package se.l4.vibe.examples;

import java.io.IOException;
import java.time.Duration;

import se.l4.vibe.JmxBackend;
import se.l4.vibe.JvmProbes;
import se.l4.vibe.LoggingBackend;
import se.l4.vibe.Vibe;
import se.l4.vibe.checks.Check;
import se.l4.vibe.checks.Conditions;
import se.l4.vibe.operations.Average;
import se.l4.vibe.sampling.Sampler;

/**
 * A simple example that will log the current CPU usage every 5 seconds.
 */
public class SamplerExample
{
	public static void main(String[] args)
		throws IOException
	{
		Vibe vibe = Vibe.builder()
			.withBackend(LoggingBackend.builder()
				.logSamples()
				.build()
			)
			.withBackend(JmxBackend.builder().build())
			.build();

		/*
		 * Sample and export JVM CPU usage.
		 */
		Sampler<Double> cpuUsage = Sampler.forProbe(JvmProbes.cpuUsage())
			.setInterval(Duration.ofSeconds(1))
			.build();

		vibe.export(cpuUsage)
			.at("jvm", "cpu")
			.done();

		/*
		 * Create a check that meets its conditions when CPU usage is below
		 * 80% over a 10 second average. Repeat events every 5 seconds while
		 * the check meets these conditions.
		 */
		Check check = Check.builder()
			.forSampler(cpuUsage)
				.apply(Average.averageOver(Duration.ofSeconds(10)))
				.is(Conditions.below(0.8))
			.whenMetRepeatEvery(Duration.ofSeconds(5))
			.build();

		check.addListener(event -> System.out.println("Check has conditions met: " + event.isConditionsMet()));

		System.out.println("Press enter to exit");
		System.in.read();
	}
}
