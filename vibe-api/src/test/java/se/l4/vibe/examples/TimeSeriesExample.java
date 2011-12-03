package se.l4.vibe.examples;

import static se.l4.vibe.trigger.Conditions.*;
import static se.l4.vibe.trigger.Triggers.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.DefaultVibe;
import se.l4.vibe.Vibe;
import se.l4.vibe.backend.JmxBackend;
import se.l4.vibe.backend.LoggingBackend;
import se.l4.vibe.event.EventSeverity;
import se.l4.vibe.probes.JvmProbes;

/**
 * A simple example that will log the current CPU usage every 5 seconds.
 * 
 * @author Andreas Holstenson
 *
 */
public class TimeSeriesExample
{
	public static void main(String[] args)
		throws IOException
	{
		Vibe vibe = DefaultVibe.builder()
			.setBackends(new LoggingBackend(), new JmxBackend())
			.setSampleInterval(2, TimeUnit.SECONDS)
			.build();
		
		/**
		 * Export a time series over JVM CPU usage. Include a trigger
		 * that will trigger if the average over 10 seconds is below 80%,
		 * which it always is.
		 */
		vibe.timeSeries(JvmProbes.cpuUsage())
			.at("jvm/cpu")
			.when(averageOver(10, TimeUnit.SECONDS), below(0.8))
				.sendEvent(EventSeverity.CRITICAL)
			.export();
		
		System.out.println("Press enter to exit");
		System.in.read();
	}
}
