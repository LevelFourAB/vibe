package se.l4.vibe.examples;

import java.io.IOException;
import java.time.Duration;

import se.l4.vibe.JvmProbes;
import se.l4.vibe.Vibe;
import se.l4.vibe.backend.JmxBackend;
import se.l4.vibe.backend.LoggingBackend;
import se.l4.vibe.operations.Average;
import se.l4.vibe.sampling.Sampler;
import se.l4.vibe.trigger.Check;
import se.l4.vibe.trigger.Conditions;

/**
 * A simple example that will log the current CPU usage every 5 seconds.
 *
 * @author Andreas Holstenson
 *
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
			.withBackend(new JmxBackend())
			.build();

		/*
		 * Sample and export JVM CPU usage.
		 */
		Sampler<Double> cpuUsage = Sampler.forProbe(JvmProbes.cpuUsage())
			.build();

		vibe.export(cpuUsage)
			.at("jvm", "cpu")
			.done();

		/*
		 * Create a check that meets its conditions when CPU usage is below
		 * 80%.
		 */
		Check check = Check.builder()
			.forSampler(cpuUsage)
				.apply(Average.averageOver(Duration.ofSeconds(10)))
				.is(Conditions.below(0.8))
			.build();

		check.addListener(event -> System.out.println("Check has conditions met: " + event.isConditionsMet()));

		System.out.println("Press enter to exit");
		System.in.read();
	}
}
