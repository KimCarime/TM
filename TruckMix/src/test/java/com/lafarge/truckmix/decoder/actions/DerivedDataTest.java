package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.common.enums.RotationDirection;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

public class DerivedDataTest extends ReadActionTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        ReadAction message = new DerivedData(callback);

        message.decode(new byte[]{0x00, 0x00, 0x00, 0x52, 0x02, 0x46});
        verify(callback, only()).derivedData(RotationDirection.MIXING, true, 82, 582);
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        ReadAction message = new DerivedData(null);
        message.decode(new byte[42]);
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_an_exception_for_a_bad_boolean_byte() {
        ReadAction message = new DerivedData(null);

        message.decode(new byte[]{0x00, 0x42, 0x00, 0x52, 0x02, 0x46});
    }
}
