package se.l4.vibe.probes;

import se.l4.vibe.timer.TimerSnapshot;

public class ValueReaders
{
	private static final ValueReader SAME = new ValueReader()
	{
		@Override
		public Object read(Object object)
		{
			return object;
		}
	};
	
	private static final ValueReader<TimerSnapshot, Long> TIMER_TOTAL_MS = new ValueReader<TimerSnapshot, Long>()
	{
		@Override
		public Long read(TimerSnapshot object)
		{
			return object.getTotalTimeInMs();
		}
	};
	
	private static final ValueReader<TimerSnapshot, Long> TIMER_TOTAL_NS = new ValueReader<TimerSnapshot, Long>()
	{
		@Override
		public Long read(TimerSnapshot object)
		{
			return object.getTotalTimeInNs();
		}
	};
	
	private static final ValueReader<TimerSnapshot, Long> TIMER_SAMPLES = new ValueReader<TimerSnapshot, Long>()
	{
		@Override
		public Long read(TimerSnapshot object)
		{
			return object.getSamples();
		}
	};
	
	private ValueReaders()
	{
	}
	
	public static <T> ValueReader<T, T> same()
	{
		return SAME;
	}
	
	public static ValueReader<TimerSnapshot, Long> forTimerSnapshotTotalTimeInMs()
	{
		return TIMER_TOTAL_MS;
	}
	
	public static ValueReader<TimerSnapshot, Long> forTimerSnapshotTotalTimeInNs()
	{
		return TIMER_TOTAL_NS;
	}
	
	public static ValueReader<TimerSnapshot, Long> forTimerSnapshotSamples()
	{
		return TIMER_SAMPLES;
	}
	
	/**
	 * Get a {@link Probe probe} that reads a value from the given object.
	 * 
	 * @param object
	 * @param reader
	 * @return
	 */
	public static <I, O> Probe<O> valueReaderProbe(I object, ValueReader<I, O> reader)
	{
		return new ValueReadingProbe<I, O>(object, reader);
	}
	
	/**
	 * Get a {@link Probe probe} that reads a value from the result of the
	 * given probe.
	 * 
	 * @param probe
	 * @param reader
	 * @return
	 */
	public static <I, O> Probe<O> valueReaderProbe(Probe<I> probe, ValueReader<I, O> reader)
	{
		return new ValueReadingProbeViaProbe<I, O>(probe, reader);
	}
	
	private static class ValueReadingProbe<Input, Output>
		implements Probe<Output>
	{
		private final Input object;
		private final ValueReader<Input, Output> reader;
	
		public ValueReadingProbe(Input object, ValueReader<Input, Output> reader)
		{
			this.object = object;
			this.reader = reader;
		}
		
		@Override
		public Output read()
		{
			return reader.read(object);
		}
	}

	private static class ValueReadingProbeViaProbe<Input, Output>
		implements Probe<Output>
	{
		private final Probe<Input> probe;
		private final ValueReader<Input, Output> reader;
	
		public ValueReadingProbeViaProbe(Probe<Input> probe, ValueReader<Input, Output> reader)
		{
			this.probe = probe;
			this.reader = reader;
		}
		
		@Override
		public Output read()
		{
			Input object = probe.read();
			return reader.read(object);
		}
	}
}
