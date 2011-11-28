package se.l4.vibe.examples;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import se.l4.vibe.DefaultVibe;
import se.l4.vibe.Vibe;
import se.l4.vibe.probes.RuntimeProbes;

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
			.setSampleInterval(5, TimeUnit.SECONDS)
			.build();
		
		vibe.timeSeries(RuntimeProbes.getCpuUsage())
			.at("sys/cpu")
			.export();
		
		System.out.println("Press enter to exit");
		System.in.read();
	}
}
