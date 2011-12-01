package se.l4.vibe.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.DefaultVibe;
import se.l4.vibe.Vibe;
import se.l4.vibe.backend.JmxBackend;
import se.l4.vibe.backend.LoggingBackend;
import se.l4.vibe.event.EventSeverity;
import se.l4.vibe.event.Events;
import se.l4.vibe.probes.Probe;

/**
 * Example showing how to track events.
 * 
 * This example will allow you to manually trigger events. It has a time series
 * that will output the number of events received during a 10 second interval.
 * 
 * @author Andreas Holstenson
 *
 */
public class TrackEventsExample
{
	public static void main(String[] args)
		throws IOException
	{
		Vibe vibe = DefaultVibe.builder()
			.setBackends(new LoggingBackend(), new JmxBackend())
			.setSampleInterval(10, TimeUnit.SECONDS)
			.build();
		
		// The events object used to send the events
		Events<AccessEvent> accessEvents = vibe.events(AccessEvent.class)
			.at("auth/events")
			.setSeverity(EventSeverity.INFO)
			.create();
		
		// Sampling the number of events sent during 10 second interval
		vibe.timeSeries(accessEvents.getEventsProbe())
			.at("auth/sampled")
			.export();
		
		// Probe for the total number of events received
		Probe<Long> total = vibe.probe(accessEvents.getTotalEventsProbe())
			.at("auth/total")
			.export();
		
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
	{
	}
}
