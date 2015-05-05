package com.lafarge.tm;

import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DecoderTest {


    @Test @Ignore
    public void decode_parts() throws IOException {

        byte[] msg1 = "AAAAAAAAAAA".getBytes("UTF-8");
        byte[] msg2 = "AAAAABBBBBB".getBytes("UTF-8");
        byte[] msg3 = "BBCCCCCCCCC".getBytes("UTF-8");

        final List<String> received = new LinkedList<>();
        MessageReceivedListener callback = new MessageReceivedListener() {
            @Override
            public void messagesReceived(String message) {
                received.add(message);
            }
        };

        Decoder decoder = new Decoder(callback);
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
    public void header_state_accepts_correct_header_and_returns_version_state() throws IOException {
        Decoder.HeaderState state = new Decoder.HeaderState();
        Decoder.State actual = state.decode(new ByteArrayInputStream(new byte[]{(byte)0xC0}));
        assertThat(actual, instanceOf(Decoder.VersionState.class));
    }

    @Test
    public void header_state_returns_itself_if_buffer_empty() throws IOException {
        Decoder.HeaderState state = new Decoder.HeaderState();
        Decoder.State actual = state.decode(new ByteArrayInputStream(new byte[0]));
        assertThat(actual, is((Decoder.State) state));
    }

    @Test
    public void header_state_reject_all_invalid_header_bytes_and_stays_current() throws IOException {
        Decoder.HeaderState state = new Decoder.HeaderState();
        ByteArrayInputStream is = new ByteArrayInputStream(new byte[]{(byte)0xC1, (byte)0xC2});
        Decoder.State actual = state.decode(is);
        assertThat(actual, is((Decoder.State)state));
        assertThat(is.read(), is(-1));
    }
}
