package com.lafarge.tm;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class TypeStateTest {

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
    public void type_state_should_accept_a_correct_message_and_return_size_state() throws IOException {
        TypeState state = new TypeState(new State.Message(), messageListener, progressListener);
        State actual;

        // TRAME_SLUMP_COURANT
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x10, 0x01 }));
        assertThat(actual, instanceOf(SizeState.class));
    }

    @Test
    public void type_state_should_accept_first_byte_but_not_second_and_return_header_state() throws IOException {
        TypeState state = new TypeState(new State.Message(), messageListener, progressListener);
        // First byte of TRAME_SLUMP_COURANT and an unknown byte
        State actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x10, 0x42 }));
        assertThat(actual, instanceOf(HeaderState.class));
    }

    @Test
    public void type_state_should_accept_first_then_second_byte_and_return_size_state() throws IOException {
        TypeState state = new TypeState(new State.Message(), messageListener, progressListener);
        // First byte of TRAME_SLUMP_COURANT
        State actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x10 }));
        assertThat(actual, is((State) state));

        // Second byte of TRAME_SLUMP_COURANT
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x01 }));
        assertThat(actual, instanceOf(SizeState.class));
    }

    @Test
    public void type_state_should_accept_first_then_refuse_second_byte_and_return_header_state() throws IOException {
        TypeState state = new TypeState(new State.Message(), messageListener, progressListener);
        State actual;

        // First byte of TRAME_SLUMP_COURANT
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x10 }));
        assertThat(actual, is((State) state));

        // Unknown byte
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x42 }));
        assertThat(actual, instanceOf(HeaderState.class));
    }

    @Test
    public void type_state_should_refuse_first_byte_and_return_header_state() throws IOException {
        TypeState state = new TypeState(new State.Message(), messageListener, progressListener);
        // Unknown byte
        State actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x42 }));
        assertThat(actual, instanceOf(HeaderState.class));
    }
}
