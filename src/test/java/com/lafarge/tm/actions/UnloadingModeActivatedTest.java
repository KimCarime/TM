package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;

import static org.mockito.Mockito.*;

public class UnloadingModeActivatedTest extends ReadActionTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        ReadAction message = new UnloadingModeActivated(callback);

        message.decode(null);
        verify(callback, only()).unloadingModeActivated();
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        ReadAction message = new UnloadingModeActivated(null);

        message.decode(new byte[42]);
    }
}