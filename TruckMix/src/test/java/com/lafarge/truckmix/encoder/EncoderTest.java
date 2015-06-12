package com.lafarge.truckmix.encoder;

import com.lafarge.truckmix.encoder.listeners.MessageSentListener;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;

public class EncoderTest {

    private Encoder encoder;
    private MessageSentListener messageSentListener;

    @Before
    public void setup() {
        this.messageSentListener = mock(MessageSentListener.class);
        this.encoder = new Encoder(messageSentListener);
    }

    @Test
    public void encodeTargetSlump_should_return_correct_bytes() throws IOException {
        byte[] result = this.encoder.targetSlump(42);
        assertArrayEquals(result, new byte[]{(byte) 0xC0, 0x01, (byte) 0x80, 0x01, 0x00, 0x02, 0x00, 0x2A, 0x5E, (byte) 0x84});
    }
}
