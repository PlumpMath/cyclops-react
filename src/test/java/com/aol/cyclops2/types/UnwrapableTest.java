package com.aol.cyclops2.types;

import cyclops.control.Xor;
import cyclops.control.lazy.Either;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class UnwrapableTest {
    @Test
    public void unwrapIfInstanceHit() throws Exception {
       Object o = new MyUnWrappable().unwrapIfInstance(Xor.class,()->"hello");
       assertThat(o,equalTo(Either.left("hello")));
    }

    @Test
    public void unwrapIfInstanceMiss() throws Exception {
        Object o = new MyUnWrappable().unwrapIfInstance(String.class,()->"hello");
        assertThat(o,equalTo("hello"));
    }
    static class MyUnWrappable implements Unwrapable{
        @Override
        public <R> R unwrap() {
            return (R)Either.left("hello");
        }
    }
}
