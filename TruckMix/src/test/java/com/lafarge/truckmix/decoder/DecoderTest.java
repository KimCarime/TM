package com.lafarge.truckmix.decoder;

import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.decoder.Decoder;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.ProgressListener;
import com.lafarge.truckmix.decoder.states.HeaderState;
import com.lafarge.truckmix.decoder.states.TypeState;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DecoderTest {

    private MessageReceivedListener messageListener;
    private ProgressListener progressListener;
    private Decoder decoder;

    @Before
    public void setup() {
        messageListener = mock(MessageReceivedListener.class);
        progressListener = mock(ProgressListener.class);
        this.decoder = new Decoder(messageListener, progressListener);
    }

    @Test
    public void should_decode_message() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0xC0); // Header
        out.write(0x01); // Version
        out.write(0x10); // TRAME_SLUMP_COURANT
        out.write(0x01);
        out.write(0x00); // Size
        out.write(0x02);
        out.write(0x00); // Data
        out.write(0xEE);
        out.write(0x42); // CRC
        out.write(0x47);

        decoder.decode(out.toByteArray());
        verify(messageListener).slumpUpdated(238);
    }

    /**
     * Expiration
     */
    @Test
    public void current_state_should_expire_after_one_second_between_two_decode() throws NoSuchFieldException, IllegalAccessException, IOException {

        decoder.decode(new byte[]{(byte) Protocol.HEADER});

        Field dateField = Decoder.class.getDeclaredField("lastDecode");
        dateField.setAccessible(true);
        dateField.set(decoder, System.currentTimeMillis() - (Decoder.STATE_EXPIRATION_DELAY_MILLIS + 1));

        decoder.decode(new byte[]{(byte) Protocol.VERSION});

        Field stateField = Decoder.class.getDeclaredField("state");
        stateField.setAccessible(true);
        assertThat(stateField.get(decoder), instanceOf(HeaderState.class));
    }

    @Test
    public void current_state_should_not_expire_between_two_decode() throws NoSuchFieldException, IllegalAccessException, IOException {

        decoder.decode(new byte[]{(byte) Protocol.HEADER});
        decoder.decode(new byte[]{(byte) Protocol.VERSION});

        Field stateField = Decoder.class.getDeclaredField("state");
        stateField.setAccessible(true);
        assertThat(stateField.get(decoder), instanceOf(TypeState.class));
    }
}