package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

public class DeliveryParametersReceivedTest extends ReadActionTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        ReadAction message = new DeliveryParametersReceived(callback);

        message.decode(new byte[0]);
        verify(callback, only()).deliveryParametersReceived();
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        ReadAction message = new DeliveryParametersReceived(null);

        message.decode(new byte[42]);
    }
}
