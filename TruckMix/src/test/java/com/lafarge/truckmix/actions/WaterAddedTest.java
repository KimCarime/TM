package com.lafarge.truckmix.actions;

import com.lafarge.truckmix.MessageReceivedListener;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class WaterAddedTest extends ReadActionTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        ReadAction message = new WaterAdded(callback);

        message.decode(new byte[]{0x2A, 0x00});
        verify(callback, only()).waterAdded(42, MessageReceivedListener.WaterAdditionMode.MANUAL);
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        ReadAction message = new WaterAdded(null);

        message.decode(new byte[42]);
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_an_exception_for_a_bad_boolean_byte() {
        ReadAction message = new WaterAdded(null);

        message.decode(new byte[]{0x42, 0x44});
    }
}
