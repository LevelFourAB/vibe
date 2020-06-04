package se.l4.vibe.probes;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link Average}.
 *
 * @author Andreas Holstenson
 *
 */
public class AverageTest
{
	@Test
	public void testEmtpy()
	{
		SampleOperation<Number, Double> op = Average.newOperation();

		if(! Double.isNaN(op.get()))
		{
			throw new AssertionError("Expected result to be NaN");
		}
	}

	@Test
	public void testAdd()
	{
		TimeSeriesOperationHelper<Number, Double> helper =
			TimeSeriesOperationHelper.create(Average.newOperation());

		helper.add(5);

		assertThat(helper.get(), is(5.0));
	}

	@Test
	public void testAddMultiple()
	{
		TimeSeriesOperationHelper<Number, Double> helper =
			TimeSeriesOperationHelper.create(Average.newOperation());

		helper.add(5);
		helper.add(15);

		assertThat(helper.get(), is(10.0));
	}

	@Test
	public void testAddRemove()
	{
		TimeSeriesOperationHelper<Number, Double> helper =
			TimeSeriesOperationHelper.create(Average.newOperation());

		helper.add(5);
		helper.add(15);

		helper.removeFirst();

		assertThat(helper.get(), is(15.0));
	}
}
