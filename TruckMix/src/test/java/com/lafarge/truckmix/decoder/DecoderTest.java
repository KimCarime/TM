package com.lafarge.truckmix.decoder;

import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.ProgressListener;
import com.lafarge.truckmix.decoder.states.HeaderState;
import com.lafarge.truckmix.decoder.states.TypeState;
import com.lafarge.truckmix.utils.MessageReceivedFactory;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DecoderTest {

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
    public void should_decode_message() throws IOException {
        decoder.decode(MessageReceivedFactory.createSlumpUpdatedMessage());
        verify(messageListener).slumpUpdated(238);
    }

    /**
     * Expiration
     */
    @Test
    public void current_state_should_expire_after_one_second_between_two_decode() throws NoSuchFieldException, IllegalAccessException, IOException {

        decoder.decode(new byte[]{(byte) Protocol.HEADER});

        Field dateField = Decoder.class.getDeclaredField("lastDecode");
        dateField.setAccessible(true);
        dateField.set(decoder, System.currentTimeMillis() - (Decoder.STATE_EXPIRATION_DELAY_MILLIS + 1));

        decoder.decode(new byte[]{(byte) Protocol.VERSION});

        Field stateField = Decoder.class.getDeclaredField("state");
        stateField.setAccessible(true);
        assertThat(stateField.get(decoder), instanceOf(HeaderState.class));
    }

    @Test
    public void current_state_should_not_expire_between_two_decode() throws NoSuchFieldException, IllegalAccessException, IOException {

        decoder.decode(new byte[]{(byte) Protocol.HEADER});
        decoder.decode(new byte[]{(byte) Protocol.VERSION});

        Field stateField = Decoder.class.getDeclaredField("state");
        stateField.setAccessible(true);
        assertThat(stateField.get(decoder), instanceOf(TypeState.class));
    }
}
