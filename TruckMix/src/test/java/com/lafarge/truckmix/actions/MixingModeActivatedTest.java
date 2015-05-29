package com.lafarge.truckmix.actions;

import com.lafarge.truckmix.MessageReceivedListener;

import static org.mockito.Mockito.*;

public class MixingModeActivatedTest extends ReadActionTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        ReadAction message = new MixingModeActivated(callback);

        message.decode(null);
        verify(callback, only()).mixingModeActivated();
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        ReadAction message = new MixingModeActivated(null);

        message.decode(new byte[42]);
    }
}
