package com.lafarge.truckmix.decoder.states;

import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.ProgressListener;

import java.io.IOException;
import java.io.InputStream;

/**
 * The second state of a message. Will return a TypeState if we pass a valid byte, otherwise
 * will return HeaderState.
 */
public final class VersionState extends State {

    /**
     * Constructs a VersionState
     *
     * @see State(Message, MessageReceivedListener, ProgressListener)
     */
    public VersionState(Message message, MessageReceivedListener messageListener, ProgressListener progressListener) {
        super(message, messageListener, progressListener);
    }

    @Override
    public State decode(InputStream in) throws IOException {
        int read = in.read();

        if (read == -1) {
            return this;
        } else {
            progressListener.willProcessByte(ProgressListener.ProgressState.STATE_VERSION, (byte) read);
            if (read == Protocol.VERSION) {
                saveBuffer();
                return new TypeState(message, messageListener, progressListener).decode(in);
            } else {
                progressListener.parsingFailed(ProgressListener.ParsingError.ERROR_PARSING_VERSION, (byte) read);
                return new HeaderState(messageListener, progressListener).decode(in);
            }
        }
    }

    @Override
    protected void saveBuffer() {
        this.message.version = (byte) Protocol.VERSION;
    }
}
