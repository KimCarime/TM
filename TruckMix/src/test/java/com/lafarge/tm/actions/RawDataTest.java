package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.mockito.Mockito.*;

public class RawDataTest extends ReadActionTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        ReadAction message = new RawData(callback);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0x00);
        out.write(0x00);
        out.write(0x00);
        out.write(0x3A);
        out.write(0x00);
        out.write(0x00);
        out.write(0x02);
        out.write(0xCC);
        out.write(0x00);
        out.write(0x00);
        out.write(0x01);
        out.write(0xF2);
        out.write(0xFF);

        message.decode(out.toByteArray());
        verify(callback, only()).rawData(58, 716, 498, false);
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        ReadAction message = new RawData(null);

        message.decode(new byte[42]);
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_an_exception_for_a_bad_boolean_byte() {
        ReadAction message = new RawData(null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0x00);
        out.write(0x00);
        out.write(0x00);
        out.write(0x3A);
        out.write(0x00);
        out.write(0x00);
        out.write(0x02);
        out.write(0xCC);
        out.write(0x00);
        out.write(0x00);
        out.write(0x01);
        out.write(0xF2);
        out.write(0x42); // This byte should trigger the exception because it should only be equal 0x00 or 0xFF

        message.decode(out.toByteArray());
    }
}
