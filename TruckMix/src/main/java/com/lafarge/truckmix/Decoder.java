package com.lafarge.truckmix;

import com.lafarge.truckmix.states.HeaderState;
import com.lafarge.truckmix.states.State;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Decoder {

    public static final int STATE_EXPIRATION_DELAY_MILLIS = 1000;

    private final MessageReceivedListener messageListener;
    private final ProgressListener progressListener;

    private State state;
    private long lastDecode;

    public Decoder(MessageReceivedListener messageListener, ProgressListener progressListener) {
        this.messageListener = messageListener;
        this.progressListener = progressListener;
        this.state = new HeaderState(messageListener, progressListener);
        this.lastDecode = -1;
    }

    public void decode(byte[] in) throws IOException {
        if (progressListener != null) {
            progressListener.willDecode(in);
        }
        boolean expired = this.lastDecode > 0 && (System.currentTimeMillis() - this.lastDecode > STATE_EXPIRATION_DELAY_MILLIS);
        this.lastDecode = System.currentTimeMillis();
        if (expired && !(state instanceof HeaderState)) {
            if (progressListener != null) {
                progressListener.timeout();
            }
            this.state = new HeaderState(messageListener, progressListener).decode(new ByteArrayInputStream(in));
        } else {
            this.state = this.state.decode(new ByteArrayInputStream(in));
        }
    }
}
