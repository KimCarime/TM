package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class SensorOutputConnectionChangedTest extends ReadActionTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        ReadAction message = new SensorOutputConnectionChanged(callback);

        message.decode(new byte[]{(byte) 0xFF});
        verify(callback, only()).outputSensorConnectionChanged(false);
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        ReadAction message = new SensorOutputConnectionChanged(null);

        message.decode(new byte[42]);
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_an_exception_for_a_bad_boolean_byte() {
        ReadAction message = new SensorOutputConnectionChanged(null);

        message.decode(new byte[]{0x42});
    }
}
