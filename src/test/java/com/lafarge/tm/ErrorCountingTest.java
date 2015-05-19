package com.lafarge.tm;

import static org.mockito.Mockito.*;

public class ErrorCountingTest extends MessageTypeTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        MessageType message = new ErrorCounting(callback);

        message.decode(null);
        verify(callback, only()).countingError();
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        MessageType message = new ErrorCounting(null);

        message.decode(new byte[42]);
    }
}
