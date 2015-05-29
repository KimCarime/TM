package com.lafarge.truckmix.actions;

import com.lafarge.truckmix.MessageReceivedListener;

import static org.mockito.Mockito.*;

public class WaterAdditionRequestTest extends ReadActionTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        ReadAction message = new WaterAdditionRequest(callback);

        message.decode(new byte[]{0x0B});
        verify(callback, only()).waterAdditionRequest(11);
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        ReadAction message = new WaterAdditionRequest(null);

        message.decode(new byte[42]);
    }
}
