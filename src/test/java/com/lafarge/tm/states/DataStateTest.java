package com.lafarge.tm.states;

import com.lafarge.tm.Decoder;
import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.ProgressListener;
import com.lafarge.tm.Protocol;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class DataStateTest {

    private MessageReceivedListener messageListener;
    private ProgressListener progressListener;

    @Before
    public void setup() {
        messageListener = mock(MessageReceivedListener.class);
        progressListener = mock(ProgressListener.class);
    }

    @Test
    public void data_state_should_accept_bytes_until_the_expected_nb_bytes_is_reach_and_return_crc_state() throws IOException {
        Protocol.Spec spec = Protocol.constants.get(Protocol.TRAME_DONNEES_CALIBRATION);
        DataState state = new DataState(spec.address, spec.size, new State.Message(), messageListener, progressListener);

        State actual = null;
        for (int i = 0; i < spec.size; i++) {
            actual = state.decode(new ByteArrayInputStream(new byte[]{0x42}));
            if (i < spec.size - 1) {
                assertThat(actual, is((State) state));
            }
        }
        assertThat(actual, instanceOf(CrcState.class));
    }

    @Test
    public void data_state_should_accept_correct_byte_and_return_crc_state() throws IOException {
        Protocol.Spec spec = Protocol.constants.get(Protocol.TRAME_VOLUME_EAU_AJOUTE_PLUS_MODE);
        DataState state = new DataState(spec.address, spec.size, new State.Message(), messageListener, progressListener);
        State actual = state.decode(new ByteArrayInputStream(new byte[]{0x21, 0x00}));
        assertThat(actual, instanceOf(CrcState.class));
    }

    @Test
    public void data_state_should_refuse_byte_and_return_header_state() throws IOException {
        Protocol.Spec spec = Protocol.constants.get(Protocol.TRAME_VOLUME_EAU_AJOUTE_PLUS_MODE);
        DataState state = new DataState(spec.address, spec.size, new State.Message(), messageListener, progressListener);
        State actual = state.decode(new ByteArrayInputStream(new byte[]{0x21, 0x42}));
        assertThat(actual, instanceOf(HeaderState.class));
    }
}
