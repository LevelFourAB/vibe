package se.l4.vibe.internal;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

import se.l4.vibe.ListenerHandle;
import se.l4.vibe.Listeners;
import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.SampleOperation;
import se.l4.vibe.sampling.Sampler;
import se.l4.vibe.trigger.Check;
import se.l4.vibe.trigger.CheckEvent;
import se.l4.vibe.trigger.CheckListener;
import se.l4.vibe.trigger.Condition;

public class CheckImpl<Input>
	implements Check
{
	private final Listeners<CheckListener> listeners;

	private final Sampler<Input> sampler;
	private final Condition<Input> condition;

	private ListenerHandle listenerHandle;

	public CheckImpl(
		Sampler<Input> sampler,
		Condition<Input> condition
	)
	{
		this.sampler = sampler;
		this.condition = condition;

		listeners = new Listeners<>(size -> {
			if(size == 1)
			{
				listenerHandle = sampler.addListener(this::check);
			}
			else if(size == 0)
			{
				listenerHandle.remove();
			}
		});
	}

	@Override
	public boolean isConditionsMet()
	{
		Input input = sampler.read();
		return condition.matches(input);
	}

	@Override
	public ListenerHandle addListener(CheckListener listener)
	{
		return listeners.add(listener);
	}

	@Override
	public void removeListener(CheckListener listener)
	{
		listeners.remove(listener);
	}

	private void check(Sample<Input> sample)
	{
		CheckEvent event;
		if(condition.matches(sample.getValue()))
		{
			// TODO: Detect if repeat
			event = new CheckEvent(true, false, null);
		}
		else
		{
			event = new CheckEvent(true, false, null);
		}

		listeners.forEach(l -> l.checkStatus(event));
	}

	public static class BuilderImpl
		implements Builder
	{
		private Sampler<?> sampler;
		private Condition<?> condition;

		@Override
		public <I> SamplerWhenBuilder<I> forSampler(Sampler<I> sampler)
		{
			return new SamplerBuilderImpl<>(sampler, this::receiveResult);
		}

		@Override
		public ConditionWhenBuilder forSupplier(BooleanSupplier supplier)
		{
			return new BooleanSupplierBuilder(supplier, this::receiveResult);
		}

		private Builder receiveResult(Sampler<?> sampler, Condition<?> condition)
		{
			this.sampler = sampler;
			this.condition = condition;
			return this;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Check build()
		{
			return new CheckImpl(sampler, condition);
		}
	}

	private static class SamplerBuilderImpl<I>
		implements SamplerWhenBuilder<I>
	{
		private final BiFunction<Sampler<?>, Condition<?>, Builder> resultReceiver;
		private Sampler<I> sampler;

		public SamplerBuilderImpl(
			Sampler<I> sampler,
			BiFunction<Sampler<?>, Condition<?>, Builder> resultReceiver
		)
		{
			this.sampler = sampler;
			this.resultReceiver = resultReceiver;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <O> SamplerWhenBuilder<O> apply(SampleOperation<I, O> operation)
		{
			Objects.requireNonNull(operation, "operation can not be null");

			sampler = (Sampler) sampler.apply(operation);
			return (SamplerWhenBuilder) this;
		}

		@Override
		public Builder is(Condition<I> condition)
		{
			Objects.requireNonNull(condition, "condition can not be null");
			return resultReceiver.apply(sampler, condition);
		}
	}

	private static class BooleanSupplierBuilder
		implements ConditionWhenBuilder
	{
		private final BiFunction<Sampler<?>, Condition<?>, Builder> resultReceiver;

		private final BooleanSupplier supplier;
		private Duration checkInterval = Duration.ofMinutes(1);

		public BooleanSupplierBuilder(
			BooleanSupplier supplier,
			BiFunction<Sampler<?>, Condition<?>, Builder> resultReceiver
		)
		{
			this.supplier = supplier;
			this.resultReceiver = resultReceiver;
		}

		@Override
		public ConditionWhenBuilder setCheckInterval(long time, TimeUnit unit)
		{
			this.checkInterval = Duration.ofMillis(unit.toMillis(time));
			return this;
		}

		@Override
		public Builder done()
		{
			Sampler<Boolean> sampler = Sampler.forProbe(supplier::getAsBoolean)
				.setInterval(checkInterval)
				.build();

			Condition<Boolean> condition = v -> v;

			return resultReceiver.apply(sampler, condition);
		}
	}
}
