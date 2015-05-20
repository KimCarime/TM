package com.lafarge.tm.actions;

import org.junit.Test;

public abstract class ReadActionTest {
    @Test
    public abstract void should_trigger_callback_with_correct_values();

    @Test(expected = RuntimeException.class)
    public abstract void should_throw_an_exception_if_data_length_is_not_conform_to_protocol();
}
