package se.l4.vibe.probes;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for minimum range.
 * 
 * @author Andreas Holstenson
 *
 */
public class MinRangeTest
{
	@Test
	public void testMin()
	{
		TimeSeriesOperationHelper<Number, Double> helper = 
			TimeSeriesOperationHelper.create(Range.newMinimumOperation());
		
		helper.add(1);
		
		assertThat(helper.get(), is(1.0));
	}
	
	@Test
	public void testMinMultiple()
	{
		TimeSeriesOperationHelper<Number, Double> helper = 
			TimeSeriesOperationHelper.create(Range.newMinimumOperation());
		
		helper.add(4);
		helper.add(1);
		
		assertThat(helper.get(), is(1.0));
	}
	
	@Test
	public void testMinRemove()
	{
		TimeSeriesOperationHelper<Number, Double> helper = 
			TimeSeriesOperationHelper.create(Range.newMinimumOperation());
		
		helper.add(1);
		helper.add(4);
		
		helper.removeFirst();
		
		helper.add(5);
		
		assertThat(helper.get(), is(4.0));
	}
}
