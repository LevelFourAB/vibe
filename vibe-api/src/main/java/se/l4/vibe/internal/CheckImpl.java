package se.l4.vibe.internal;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import se.l4.vibe.Handle;
import se.l4.vibe.checks.Check;
import se.l4.vibe.checks.CheckEvent;
import se.l4.vibe.checks.CheckListener;
import se.l4.vibe.operations.Operation;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.SampledProbe;
import se.l4.vibe.sampling.TimeSampler;

public class CheckImpl<Input>
	implements Check
{
	private final Listeners<CheckListener> listeners;

	private final TimeSampler<Input> sampler;
	private final Predicate<Input> condition;
	private final RepetitionGuard metRepetitionGuard;
	private final RepetitionGuard unmetRepetitionGuard;

	private Handle listenerHandle;

	private boolean isConditionsMet;
	private Instant lastConditionsChange;

	public CheckImpl(
		TimeSampler<Input> sampler,
		Predicate<Input> condition,
		RepetitionGuard metRepetitionGuard,
		RepetitionGuard unmetRepetitionGuard
	)
	{
		this.sampler = sampler;
		this.condition = condition;
		this.metRepetitionGuard = metRepetitionGuard;
		this.unmetRepetitionGuard = unmetRepetitionGuard;

		listeners = new Listeners<>(size -> {
			if(size == 1)
			{
				listenerHandle = sampler.addListener(this::check);
			}
			else if(size == 0)
			{
				listenerHandle.release();
			}
		});
	}

	@Override
	public boolean isConditionsMet()
	{
		Input input = sampler.read();
		return condition.test(input);
	}

	@Override
	public Handle start()
	{
		return addListener(event -> {});
	}

	@Override
	public Handle addListener(CheckListener listener)
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
		CheckEvent event = null;
		if(condition.test(sample.getValue()))
		{
			if(! isConditionsMet)
			{
				/*
				 * Conditions are now being met, switch the state of this
				 * check and start checking for repetitions.
				 */
				lastConditionsChange = Instant.ofEpochMilli(sample.getTime());
				metRepetitionGuard.start(sample.getTime());

				event = new CheckEvent(true, false, lastConditionsChange);
				isConditionsMet = true;
			}
			else if(metRepetitionGuard.checkIfShouldRepeat(sample.getTime()))
			{
				/*
				 * Repetition guard indicates this should repeat so create an
				 * event to trigger.
				 */
				event = new CheckEvent(true, true, lastConditionsChange);
			}
		}
		else
		{
			if(isConditionsMet)
			{
				/*
				 * Conditions are no longer being met.
				 */
				lastConditionsChange = Instant.ofEpochMilli(sample.getTime());
				unmetRepetitionGuard.start(sample.getTime());

				event = new CheckEvent(false, false, lastConditionsChange);
				isConditionsMet = false;
			}
			else if(unmetRepetitionGuard.checkIfShouldRepeat(sample.getTime()))
			{
				/*
				 * Repetition guard indicates this should repeat so create an
				 * event to trigger.
				 */
				event = new CheckEvent(false, true, lastConditionsChange);
			}
		}

		if(event != null)
		{
			CheckEvent event0 = event;
			listeners.forEach(l -> l.checkStatus(event0));
		}
	}

	public static class BuilderImpl
		implements Builder
	{
		private TimeSampler<?> sampler;
		private Predicate<?> condition;
		private RepetitionGuard metRepetitionGuard = RepetitionGuard.once();
		private RepetitionGuard unmetRepetitionGuard = RepetitionGuard.once();

		@Override
		public <I> SamplerWhenBuilder<I> whenTimeSampler(TimeSampler<I> sampler)
		{
			return new SamplerBuilderImpl<>(sampler, this::receiveResult);
		}

		@Override
		public <I> ProbeWhenBuilder<I> whenProbe(Probe<I> probe)
		{
			return new ProbeBuilderImpl<>(SampledProbe.over(probe), this::receiveResult);
		}

		@Override
		public <I> ProbeWhenBuilder<I> whenProbe(SampledProbe<I> probe)
		{
			return new ProbeBuilderImpl<>(probe, this::receiveResult);
		}

		@Override
		public BooleanSupplierWhenBuilder whenSupplier(BooleanSupplier supplier)
		{
			return new BooleanSupplierBuilder(supplier, this::receiveResult);
		}

		private Builder receiveResult(TimeSampler<?> sampler, Predicate<?> condition)
		{
			this.sampler = sampler;
			this.condition = condition;
			return this;
		}

		@Override
		public Builder whenMetRepeatEvery(Duration duration)
		{
			Objects.requireNonNull(duration, "duration must not be null");

			metRepetitionGuard = RepetitionGuard.afterDuration(duration);
			return this;
		}

		@Override
		public Builder whenUnmetRepeatEvery(Duration duration)
		{
			Objects.requireNonNull(duration, "duration must not be null");

			unmetRepetitionGuard = RepetitionGuard.afterDuration(duration);
			return this;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Check build()
		{
			Objects.requireNonNull(sampler, "Check requires a condition");

			return new CheckImpl(
				sampler,
				condition,
				metRepetitionGuard,
				unmetRepetitionGuard
			);
		}
	}

	private static class SamplerBuilderImpl<I>
		implements SamplerWhenBuilder<I>
	{
		private final BiFunction<TimeSampler<?>, Predicate<?>, Builder> resultReceiver;
		private TimeSampler<I> sampler;

		public SamplerBuilderImpl(
			TimeSampler<I> sampler,
			BiFunction<TimeSampler<?>, Predicate<?>, Builder> resultReceiver
		)
		{
			this.sampler = sampler;
			this.resultReceiver = resultReceiver;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <O> SamplerWhenBuilder<O> apply(Operation<I, O> operation)
		{
			Objects.requireNonNull(operation, "operation can not be null");

			sampler = (TimeSampler) sampler.apply(operation);
			return (SamplerWhenBuilder) this;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <O> SamplerWhenBuilder<O> applyResampling(Operation<Sample<I>, Sample<O>> operation)
		{
			Objects.requireNonNull(operation, "operation can not be null");

			sampler = (TimeSampler) sampler.applyResampling(operation);
			return (SamplerWhenBuilder) this;
		}

		@Override
		public Builder is(Predicate<I> condition)
		{
			Objects.requireNonNull(condition, "condition can not be null");
			return resultReceiver.apply(sampler, condition);
		}
	}

	private static class ProbeBuilderImpl<I>
		implements ProbeWhenBuilder<I>
	{
		private final BiFunction<TimeSampler<?>, Predicate<?>, Builder> resultReceiver;

		private final TimeSampler.Builder<I> builder;

		public ProbeBuilderImpl(
			SampledProbe<I> probe,
			BiFunction<TimeSampler<?>, Predicate<?>, Builder> resultReceiver
		)
		{
			this.resultReceiver = resultReceiver;

			this.builder = TimeSampler.forProbe(probe)
				.withInterval(Duration.ofMinutes(1));
		}

		@Override
		public ProbeWhenBuilder<I> setCheckInterval(Duration duration)
		{
			Objects.requireNonNull(duration, "duration can not be null");
			this.builder.withInterval(duration);
			return this;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <O> ProbeWhenBuilder<O> apply(Operation<I, O> operation)
		{
			Objects.requireNonNull(operation, "operation can not be null");

			builder.apply(operation);
			return (ProbeWhenBuilder) this;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <O> ProbeWhenBuilder<O> applyResampling(Operation<Sample<I>, Sample<O>> operation)
		{
			Objects.requireNonNull(operation, "operation can not be null");

			builder.applyResampling(operation);
			return (ProbeWhenBuilder) this;
		}

		@Override
		public Builder is(Predicate<I> condition)
		{
			Objects.requireNonNull(condition, "condition can not be null");
			return resultReceiver.apply(builder.build(), condition);
		}
	}

	private static class BooleanSupplierBuilder
		implements BooleanSupplierWhenBuilder
	{
		private final BiFunction<TimeSampler<?>, Predicate<?>, Builder> resultReceiver;

		private final BooleanSupplier supplier;
		private Duration checkInterval;

		public BooleanSupplierBuilder(
			BooleanSupplier supplier,
			BiFunction<TimeSampler<?>, Predicate<?>, Builder> resultReceiver
		)
		{
			this.supplier = supplier;
			this.resultReceiver = resultReceiver;

			checkInterval = Duration.ofMinutes(1);
		}

		@Override
		public BooleanSupplierWhenBuilder setCheckInterval(Duration duration)
		{
			Objects.requireNonNull(duration, "duration must not be null");

			this.checkInterval = duration;
			return this;
		}

		@Override
		public Builder done()
		{
			TimeSampler<Boolean> sampler = TimeSampler.forProbe(supplier::getAsBoolean)
				.withInterval(checkInterval)
				.build();

			Predicate<Boolean> condition = v -> v;

			return resultReceiver.apply(sampler, condition);
		}
	}
}
