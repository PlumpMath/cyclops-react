package com.aol.cyclops2.react.stream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.aol.cyclops2.react.Status;

public class StatusTest {

    @Test
    public void testGetMillis() {
        Status status = new Status(0, 0, 0, 1000000L, null);
        assertThat(status.getElapsedMillis(), is(1L));
    }
}
