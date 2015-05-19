package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;

import static org.mockito.Mockito.*;

public class TruckParametersRequestTest extends MessageTypeTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        MessageType message = new TruckParametersRequest(callback);

        message.decode(null);
        verify(callback, only()).truckParametersRequest();
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        MessageType message = new TruckParametersRequest(null);

        message.decode(new byte[42]);
    }
}
