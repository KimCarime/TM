package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

public class SensorSpeedThresholdMinTest extends ReadActionTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        ReadAction message = new SensorSpeedThresholdMin(callback);

        message.decode(new byte[]{(byte) 0xFF});
        verify(callback, only()).speedSensorHasExceedMinThreshold(true);
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        ReadAction message = new SensorSpeedThresholdMin(null);

        message.decode(new byte[42]);
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_an_exception_for_a_bad_boolean_byte() {
        ReadAction message = new SensorSpeedThresholdMin(null);

        message.decode(new byte[]{0x42});
    }
}
