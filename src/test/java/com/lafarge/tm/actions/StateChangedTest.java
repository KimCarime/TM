package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;

import static org.mockito.Mockito.*;

public class StateChangedTest extends MessageTypeTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        MessageType message = new StateChanged(callback);

        message.decode(new byte[]{0x06, 0x00});
        verify(callback, only()).stateChanged(6, 0);
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        MessageType message = new StateChanged(null);

        message.decode(new byte[42]);
    }
}
