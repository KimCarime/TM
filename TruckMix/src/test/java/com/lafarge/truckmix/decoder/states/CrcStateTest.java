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
import static org.mockito.Mockito.verify;

public class CrcStateTest {

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

    @Test
    public void crc_state_should_refuse_crc_and_return_header_state() throws IOException {
        State.Message message = new State.Message();
        message.header = (byte) Protocol.HEADER;
        message.version = (byte) Protocol.VERSION;
        message.typeMsb = 0x10;
        message.typeLsb = 0x01;
        message.sizeMsb = 0x00;
        message.sizeLsb = 0x02;
        message.data = new byte[]{0x00, (byte) 0xEE};

        CrcState state = new CrcState(message, messageListener, progressListener);
        State actual = state.decode(new ByteArrayInputStream(new byte[]{(byte) 0x42, (byte) 0x42}));
        assertThat(actual, instanceOf(HeaderState.class));
    }

    @Test
    public void crc_state_should_accept_crc_and_return_null() throws IOException {
        State.Message message = new State.Message();
        message.header = (byte) Protocol.HEADER;
        message.version = (byte) Protocol.VERSION;
        message.typeMsb = 0x10;
        message.typeLsb = 0x01;
        message.sizeMsb = 0x00;
        message.sizeLsb = 0x02;
        message.data = new byte[]{0x00, (byte) 0xEE};

        CrcState state = new CrcState(message, messageListener, progressListener);
        // Send a valid CRC
        State actual = state.decode(new ByteArrayInputStream(new byte[]{(byte) 0x42, (byte) 0x47}));
        assertThat(actual, instanceOf(HeaderState.class));
        verify(messageListener).slumpUpdated(238);
    }
}
