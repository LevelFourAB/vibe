package se.l4.vibe.probes;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Tests for maximum range.
 *
 * @author Andreas Holstenson
 *
 */
public class MaxRangeTest
{
	@Test
	public void testMin()
	{
		TimeSeriesOperationHelper<Number, Double> helper =
			TimeSeriesOperationHelper.create(Range.newMaximumOperation());

		helper.add(1);

		assertThat(helper.get(), is(1.0));
	}

	@Test
	public void testMaxMultiple()
	{
		TimeSeriesOperationHelper<Number, Double> helper =
			TimeSeriesOperationHelper.create(Range.newMaximumOperation());

		helper.add(4);
		helper.add(1);

		assertThat(helper.get(), is(4.0));
	}

	@Test
	public void testMaxRemove()
	{
		TimeSeriesOperationHelper<Number, Double> helper =
			TimeSeriesOperationHelper.create(Range.newMaximumOperation());

		helper.add(5);
		helper.add(1);

		helper.removeFirst();

		helper.add(4);

		assertThat(helper.get(), is(4.0));
	}
}
