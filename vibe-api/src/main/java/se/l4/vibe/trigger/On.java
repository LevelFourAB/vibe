package se.l4.vibe.trigger;

public interface On<Input, Output>
{
	<T> Trigger<Input, T> build(Trigger<Output, T> second);
}
