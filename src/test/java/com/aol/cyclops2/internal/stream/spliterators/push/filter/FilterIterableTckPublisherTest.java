package com.aol.cyclops2.internal.stream.spliterators.push.filter;


import cyclops.collections.mutable.ListX;
import cyclops.stream.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class FilterIterableTckPublisherTest extends PublisherVerification<Long>{

	public FilterIterableTckPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.fromIterable(ListX.fill(Math.min(elements,10_000),10l)).filter(i->true);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible toNested forEachAsync toNested failed Stream
		
	}
	

}
