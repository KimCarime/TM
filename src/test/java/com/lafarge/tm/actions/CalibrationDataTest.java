package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;

import java.io.ByteArrayOutputStream;

import static org.mockito.Mockito.*;

public class CalibrationDataTest extends ReadActionTest {

    @Override
    public void should_trigger_callback_with_correct_values() {
        MessageReceivedListener callback = mock(MessageReceivedListener.class);
        ReadAction message = new CalibrationData(callback);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0x41);
        out.write(0x10);
        out.write(0x7B);
        out.write(0x0E);
        out.write(0x3F);
        out.write(0x3A);
        out.write(0x8B);
        out.write(0xD1);
        out.write(0x40);
        out.write(0x9E);
        out.write(0xAE);
        out.write(0x75);

        message.decode(out.toByteArray());
        verify(callback, only()).calibrationData(9.030043f, 0.7286959f, 4.958796f);
    }

    @Override
    public void should_throw_an_exception_if_data_length_is_not_conform_to_protocol() {
        ReadAction message = new CalibrationData(null);

        message.decode(new byte[42]);
    }
}
