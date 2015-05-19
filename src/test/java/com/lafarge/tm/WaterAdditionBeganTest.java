package com.lafarge.tm;

import static org.mockito.Mockito.*;

public class WaterAdditionBeganTest extends MessageTypeTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        MessageType message = new WaterAdditionBegan(callback);

        message.decode(null);
        verify(callback, only()).waterAdditionBegan();
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        MessageType message = new WaterAdditionBegan(null);

        message.decode(new byte[42]);
    }
}
