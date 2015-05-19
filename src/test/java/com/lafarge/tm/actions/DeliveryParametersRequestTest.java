package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;

import static org.mockito.Mockito.*;

public class DeliveryParametersRequestTest extends MessageTypeTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        MessageType message = new DeliveryParametersRequest(callback);

        message.decode(null);
        verify(callback, only()).deliveryParametersRequest();
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        MessageType message = new DeliveryParametersRequest(null);

        message.decode(new byte[42]);
    }
}
