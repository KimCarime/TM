package com.lafarge.truckmix.decoder.states;

import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.ProgressListener;

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

    @Before
    public void setup() {
        messageListener = mock(MessageReceivedListener.class);
        progressListener = mock(ProgressListener.class);
    }

    @Test
    public void version_state_accepts_correct_version_and_returns_type_state() throws IOException {
        VersionState state = new VersionState(new State.Message(), messageListener, progressListener);
        State actual = state.decode(new ByteArrayInputStream(new byte[]{(byte) Protocol.VERSION}));
        assertThat(actual, instanceOf(TypeState.class));
    }
}
