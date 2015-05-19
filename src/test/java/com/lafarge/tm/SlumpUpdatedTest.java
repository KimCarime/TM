package com.lafarge.tm;

import static org.mockito.Mockito.*;

public class SlumpUpdatedTest extends MessageTypeTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        MessageType message = new SlumpUpdated(callback);

        message.decode(new byte[]{0x00, 0x2A});
        verify(callback, only()).slumpUpdated(42);
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        MessageType message = new SlumpUpdated(null);

        message.decode(new byte[42]);
    }
}
