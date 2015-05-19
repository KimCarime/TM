package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class SensorSpeedThresholdMinTest extends MessageTypeTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        MessageType message = new SensorSpeedThresholdMin(callback);

        message.decode(new byte[]{(byte)0xFF});
        verify(callback, only()).speedSensorHasExceedMinThreshold(false);
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        MessageType message = new SensorSpeedThresholdMin(null);

        message.decode(new byte[42]);
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_an_exception_for_a_bad_boolean_byte() {
        MessageType message = new SensorSpeedThresholdMin(null);

        message.decode(new byte[]{0x42});
    }
}
