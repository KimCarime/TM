package com.lafarge.tm;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

public class EncoderTest {

    private Encoder encoder;

    @Before
    public void setup() {
        this.encoder = new Encoder();
    }

    @Test
    public void encodeTargetSlump_should_return_correct_bytes() throws IOException {
        byte[] result = this.encoder.targetSlump(42);
        assertArrayEquals(result, new byte[]{(byte) 0xC0, 0x01, (byte) 0x80, 0x01, 0x00, 0x02, 0x00, 0x2A, 0x5E, (byte) 0x84});
    }
}
