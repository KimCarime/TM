package com.lafarge.truckmix.decoder;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.ProgressListener;
import com.lafarge.truckmix.decoder.states.HeaderState;
import com.lafarge.truckmix.decoder.states.State;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * This is the Decoder part of the library, this Decoder is responsible of decoding each bytes received from the Wirma.
 */
public class Decoder {

    /**
     * The expiration delay between two decode. After having received the first part of a message, if we received the
     * second part (valid or not) after that delay, the first part of the message will be trash and the current state
     * will be reset to HEADER.
     */
    public static final int STATE_EXPIRATION_DELAY_MILLIS = 1000;

    // Listeners
    private final MessageReceivedListener messageListener;
    private final ProgressListener progressListener;

    // Internal state
    private State state;
    private long lastDecode;

    /**
     * Constructor of the decoder
     *
     * @param messageListener The decoded events
     * @param progressListener The progression of the parser
     * @throws IllegalArgumentException If one of parameters is null
     */
    public Decoder(MessageReceivedListener messageListener, ProgressListener progressListener) {
        this.messageListener = messageListener;
        this.progressListener = progressListener;
        this.state = new HeaderState(messageListener, progressListener);
        this.lastDecode = -1;
    }

    /**
     * This is the entry point of the Decoder, pass a buffer of bytes to decode a message.
     * Note that a buffer can contains only a partial message, and will keep it until the next buffer is send.
     * But if the interval between two decodes exceed <code>STATE_EXPIRATION_DELAY_MILLIS</code>. The previous buffer
     * will be trash and the Decoder will wait a new valid message.
     *
     * @param in bytes to decode
     * @throws IOException
     */
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
