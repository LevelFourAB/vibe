package se.l4.vibe.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import se.l4.vibe.JmxBackend;
import se.l4.vibe.LoggingBackend;
import se.l4.vibe.Vibe;
import se.l4.vibe.events.EventData;
import se.l4.vibe.events.EventSeverity;
import se.l4.vibe.events.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.TimeSampler;

/**
 * Example showing how to track events.
 *
 * This example will allow you to manually trigger events. It has a time series
 * that will output the number of events received during a 10 second interval.
 */
public class TrackEventsExample
{
	public static void main(String[] args)
		throws IOException
	{
		Vibe vibe = Vibe.builder()
			.addBackend(LoggingBackend.builder()
				.logSamples()
				.logEvents()
				.build()
			)
			.addBackend(JmxBackend.builder().build())
			.build();

		// The events object used to send the events
		Events<AccessEvent> accessEvents = Events.<AccessEvent>builder()
			.withSeverity(EventSeverity.INFO)
			.build();

		vibe.export(accessEvents)
			.at("auth", "events")
			.done();

		// Sampling the number of events sent during 10 second interval
		TimeSampler<Long> eventsSampler = TimeSampler.forProbe(accessEvents.getEventsProbe())
			.build();

		vibe.export(eventsSampler)
			.at("auth", "sampled")
			.done();

		// Probe for the total number of events received
		Probe<Long> total = accessEvents.getTotalEventsProbe();
		vibe.export(total)
			.at("auth", "total")
			.done();

		System.out.println("Press enter to send an event, type quit to quit");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while((line = reader.readLine()) != null)
		{
			if("quit".equals(line)) System.exit(0);

			// Send an example event
			accessEvents.register(new AccessEvent());

			// Print the total
			System.out.println("Total number of events sent: " + total.read());
		}
	}

	private static class AccessEvent
		implements EventData
	{
		@Override
		public String toHumanReadable()
		{
			return "Access Event";
		}
	}
}
