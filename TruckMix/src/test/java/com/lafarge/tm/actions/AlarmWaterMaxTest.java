package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;

import static org.mockito.Mockito.*;

public class AlarmWaterMaxTest extends ReadActionTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        ReadAction message = new AlarmWaterMax(callback);

        message.decode(null);
        verify(callback, only()).alarmWaterMax();
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        ReadAction message = new AlarmWaterMax(null);

        message.decode(new byte[42]);
    }
}
