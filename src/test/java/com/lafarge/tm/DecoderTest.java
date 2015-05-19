package com.lafarge.tm;

import com.lafarge.tm.states.HeaderState;
import com.lafarge.tm.states.TypeState;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

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
    @Ignore
    public void decode_parts() throws IOException {
        byte[] msg1 = "AAAAAAAAAAA".getBytes("UTF-8");
        byte[] msg2 = "AAAAABBBBBB".getBytes("UTF-8");
        byte[] msg3 = "BBCCCCCCCCC".getBytes("UTF-8");

        final List<String> received = new LinkedList<>();

        decoder.decode(msg1);
        assertThat(received, hasSize(0));
        decoder.decode(msg2);
        assertThat(received, hasSize(1));
        assertThat(received.get(0), equalTo("AAAAAAAAAAAAAAAA"));
        decoder.decode(msg3);
        assertThat(received, hasSize(3));
        assertThat(received.get(0), equalTo("AAAAAAAAAAAAAAAA"));
        assertThat(received.get(1), equalTo("BBBBBBBB"));
        assertThat(received.get(2), equalTo("CCCCCCCCC"));
    }

    @Test
    @Ignore
    public void should_decode_message() throws IOException {
        final MessageReceivedListener callback = mock(MessageReceivedListener.class);


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
        verify(callback, only()).slumpUpdated(12);
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
