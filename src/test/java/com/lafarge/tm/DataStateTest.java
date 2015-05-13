package com.lafarge.tm;

import org.junit.Before;
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
    private Decoder decoder;

    @Before
    public void setup() {
        messageListener = mock(MessageReceivedListener.class);
        progressListener = mock(ProgressListener.class);
        this.decoder = new Decoder(messageListener, progressListener);
    }

    @Test
    public void data_state_should_accept_bytes_until_the_expected_nb_bytes_is_reach_and_return_crc_state() throws IOException {
        final int randomSize = new Random().nextInt(15) + 1;
        DataState state = new DataState(randomSize, new State.Message(), messageListener, progressListener);

        State actual = null;
        for (int i = 0; i < randomSize; i++) {
            actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x42 }));
            if (i < randomSize - 1) {
                assertThat(actual, is((State) state));
            }
        }
        assertThat(actual, instanceOf(CrcState.class));
    }

}
