package se.l4.vibe.percentile;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class BucketPercentileCounterTest
{
	@Test
	public void testBucketPlacement()
	{
		BucketPercentileCounter counter = new BucketPercentileCounter(0, 50, 100, 200, 500, 1000);
		assertThat(counter.getBucket(-1), is(-1));
		
		assertThat(counter.getBucket(0), is(0));
		assertThat(counter.getBucket(50), is(1));
		assertThat(counter.getBucket(99), is(1));
		
		assertThat(counter.getBucket(100), is(2));
		assertThat(counter.getBucket(150), is(2));
		assertThat(counter.getBucket(199), is(2));
		
		assertThat(counter.getBucket(200), is(3));
		assertThat(counter.getBucket(250), is(3));
		
		assertThat(counter.getBucket(28351758), is(5));
	}
	
	@Test
	public void testWithSomeSamples()
	{
		BucketPercentileCounter counter = new BucketPercentileCounter(0, 100, 200);
		counter.add(101);
		counter.add(102);
		counter.add(200);
		counter.add(1);
		counter.add(4);
		counter.add(400);
		
		PercentileSnapshot snapshot = counter.get();
		assertThat(snapshot.estimatePercentile(70), is(200l));
		assertThat(snapshot.estimatePercentile(95), is(-1l));
	}
}
