package com.lafarge.tm;

import static org.mockito.Mockito.*;

public class WaterAdditionLockedTest extends MessageTypeTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        MessageType message = new WaterAdditionLocked(callback);

        message.decode(null);
        verify(callback, only()).waterAdditionLocked();
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        MessageType message = new WaterAdditionLocked(null);

        message.decode(new byte[42]);
    }
}