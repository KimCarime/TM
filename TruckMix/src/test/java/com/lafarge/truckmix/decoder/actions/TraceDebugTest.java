package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class TraceDebugTest extends ReadActionTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        ReadAction message = new TraceDebug(callback);

        message.decode(new byte[]{0x43, 0x4F, 0x55, 0x43, 0x4F, 0x55});
        verify(callback, only()).traceDebug("COUCOU");
    }

    @Override
    @Test
    @Ignore ("This test is useless because TraceDebug doesn't have a defined size")
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {}
}
