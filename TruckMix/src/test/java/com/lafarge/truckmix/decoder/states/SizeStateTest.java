package com.lafarge.truckmix.decoder.states;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.ProgressListener;
import com.lafarge.truckmix.common.Protocol;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class SizeStateTest {

    private MessageReceivedListener messageListener;
    private ProgressListener progressListener;

    @Before
    public void setup() {
        messageListener = mock(MessageReceivedListener.class);
        progressListener = mock(ProgressListener.class);
    }

    @Test
    public void size_state_should_accept_a_size_that_match_with_current_type_state_and_return_data_state() throws IOException {
        SizeState state = new SizeState(Protocol.constants.get(Protocol.TRAME_DONNEES_BRUTES).address, new State.Message(), messageListener, progressListener);
        // Bytes of TRAME_DONNEES_BRUTES's size
        State actual = state.decode(new ByteArrayInputStream(new byte[]{0x00, 0x0D}));
        assertThat(actual, instanceOf(DataState.class));
    }

    @Test
    public void size_state_should_refuse_a_size_that_doesnt_match_with_current_type_state_and_return_header_state() throws IOException {
        SizeState state = new SizeState(Protocol.constants.get(Protocol.TRAME_DONNEES_BRUTES).address, new State.Message(), messageListener, progressListener);
        // First byte of TRAME_DONNEES_BRUTES's size and an unknown byte
        State actual = state.decode(new ByteArrayInputStream(new byte[]{0x00, 0x42}));
        assertThat(actual, instanceOf(HeaderState.class));
    }

    @Test
    public void size_state_should_accept_a_size_that_match_with_current_type_state_and_return_data_state_in_two_decode() throws IOException {
        SizeState state = new SizeState(Protocol.constants.get(Protocol.TRAME_DONNEES_BRUTES).address, new State.Message(), messageListener, progressListener);
        // First byte of TRAME_DONNEES_BRUTES's size
        State actual = state.decode(new ByteArrayInputStream(new byte[]{0x00}));
        assertThat(actual, is((State) state));

        // Second byte of TRAME_DONNEES_BRUTES's size
        actual = state.decode(new ByteArrayInputStream(new byte[]{0x0D}));
        assertThat(actual, instanceOf(DataState.class));
    }

    @Test
    public void size_state_should_refuse_a_size_that_doesnt_match_with_current_type_state_and_return_header_state_in_two_decode() throws IOException {
        SizeState state = new SizeState(Protocol.constants.get(Protocol.TRAME_DONNEES_BRUTES).address, new State.Message(), messageListener, progressListener);
        // First byte of TRAME_DONNEES_BRUTES 's size
        State actual = state.decode(new ByteArrayInputStream(new byte[]{0x00}));
        assertThat(actual, is((State) state));

        // Unknown byte
        actual = state.decode(new ByteArrayInputStream(new byte[]{0x42}));
        assertThat(actual, instanceOf(HeaderState.class));
    }

    @Test
    public void size_state_should_refuse_first_byte_and_return_header_state() throws IOException {
        SizeState state = new SizeState(Protocol.constants.get(Protocol.TRAME_DONNEES_BRUTES).address, new State.Message(), messageListener, progressListener);
        // Unknown byte
        State actual = state.decode(new ByteArrayInputStream(new byte[]{0x42}));
        assertThat(actual, instanceOf(HeaderState.class));
    }

    @Test
    public void size_state_should_return_crc_state_if_size_found_is_equal_to_zero_and_match_with_type_size() throws IOException {
        State.Message message = new State.Message();
        message.data = new byte[]{0x00};

        SizeState state = new SizeState(Protocol.constants.get(Protocol.TRAME_NOTIFICATION_PASSAGE_EN_MALAXAGE).address, message, messageListener, progressListener);
        // Bytes of TRAME_NOTIFICATION_PASSAGE_EN_MALAXAGE's size
        State actual = state.decode(new ByteArrayInputStream(new byte[]{0x00, 0x00}));
        assertThat(actual, instanceOf(CrcState.class));
    }
}
