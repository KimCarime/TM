package com.lafarge.tm;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class HeaderStateTest {

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
    public void header_state_accepts_correct_header_and_returns_version_state() throws IOException {
        HeaderState state = new HeaderState(messageListener, progressListener);
        State actual= state.decode(new ByteArrayInputStream(new byte[]{ (byte)Protocol.HEADER }));
        assertThat(actual, instanceOf(VersionState.class));
    }

    @Test
    public void header_state_returns_itself_if_buffer_empty() throws IOException {
        HeaderState state = new HeaderState(messageListener, progressListener);
        State actual = state.decode(new ByteArrayInputStream(new byte[0]));
        assertThat(actual, is((State) state));
    }

    @Test
    public void header_state_reject_all_invalid_header_bytes_and_stays_current() throws IOException {
        HeaderState state = new HeaderState(messageListener, progressListener);
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{ 0x42, 0x42, 0x42, 0x42 });
        State actual = state.decode(in);
        assertThat(actual, is((State)state));
        assertThat(in.read(), is(-1));
    }
}
