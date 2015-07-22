package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

/**
 * Created by klefevre on 26/06/15.
 */
public class TemperatureUpdatedTest extends ReadActionTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        ReadAction message = new TemperatureUpdated(callback);

        message.decode(new byte[]{0x42,0x2A, 0x23, 0x20});
        verify(callback, only()).temperatureUpdated(42.534300f);
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        ReadAction message = new TemperatureUpdated(null);

        message.decode(new byte[42]);
    }
}
