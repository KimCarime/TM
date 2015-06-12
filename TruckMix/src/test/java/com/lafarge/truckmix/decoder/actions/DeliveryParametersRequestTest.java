package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;

import static org.mockito.Mockito.*;

public class DeliveryParametersRequestTest extends ReadActionTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        ReadAction message = new DeliveryParametersRequest(callback);

        message.decode(new byte[0]);
        verify(callback, only()).deliveryParametersRequest();
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        ReadAction message = new DeliveryParametersRequest(null);

        message.decode(new byte[42]);
    }
}
