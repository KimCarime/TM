package com.lafarge.tm;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;

public class VersionStateTest {

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
    public void version_state_accepts_correct_version_and_returns_type_state() throws IOException {
        VersionState state = new VersionState(new State.Message(), messageListener, progressListener);
        State actual = state.decode(new ByteArrayInputStream(new byte[]{ (byte)Protocol.VERSION }));
        assertThat(actual, instanceOf(TypeState.class));
    }
}