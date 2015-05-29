package com.lafarge.truckmix.actions;

import com.lafarge.truckmix.MessageReceivedListener;

import static org.mockito.Mockito.*;

public class DeliveryValidationRequestTest extends ReadActionTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        ReadAction message = new DeliveryValidationRequest(callback);

        message.decode(null);
        verify(callback, only()).deliveryValidationRequest();
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        ReadAction message = new DeliveryValidationRequest(null);

        message.decode(new byte[42]);
    }
}
