package com.lafarge.tm;

import org.junit.Test;

import static org.mockito.Mockito.*;

public class SensorSpeedThresholdMaxTest extends MessageTypeTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        MessageType message = new SensorSpeedThresholdMax(callback);

        message.decode(new byte[]{0x00});
        verify(callback, only()).speedSensorHasExceedMaxThreshold(true);
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        MessageType message = new SensorSpeedThresholdMax(null);

        message.decode(new byte[42]);
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_an_exception_for_a_bad_boolean_byte() {
        MessageType message = new SensorSpeedThresholdMax(null);

        message.decode(new byte[]{0x42});
    }
}