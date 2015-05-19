package com.lafarge.tm;

import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Created by klefevre on 18/05/15.
 */
public class TraceDebugTest extends MessageTypeTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        MessageType message = new TraceDebug(callback);

        message.decode(new byte[]{0x43, 0x4F, 0x55, 0x43, 0x4F, 0x55});
        verify(callback, only()).traceDebug("COUCOU");
    }

    @Override
    @Test
    @Ignore
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {}
}
