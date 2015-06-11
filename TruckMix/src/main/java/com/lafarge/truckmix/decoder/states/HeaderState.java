package com.lafarge.truckmix.decoder.states;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.ProgressListener;
import com.lafarge.truckmix.common.Protocol;

import java.io.IOException;
import java.io.InputStream;

/**
 * The first state of a message. Will return a VersionState if we pass a valid byte, otherwise
 * will return itself.
 */
public final class HeaderState extends State {

    /**
     * Constructs a new HeaderState
     *
     * @see State(Message, MessageReceivedListener, ProgressListener)
     */
    public HeaderState(MessageReceivedListener messageListener, ProgressListener progressListener) {
        super(new Message(), messageListener, progressListener);
    }

    @Override
    public State decode(InputStream in) throws IOException {
        int read = in.read();

        if (read == -1) {
            return this;
        } else {
            if (progressListener != null) {
                progressListener.willProcessByte(ProgressListener.ProgressState.STATE_HEADER, (byte) read);
            }
            if (read == Protocol.HEADER) {
                saveBuffer();
                return new VersionState(message, messageListener, progressListener).decode(in);
            } else {
                if (progressListener != null) {
                    progressListener.parsingFailed(ProgressListener.ParsingError.ERROR_PARSING_HEADER, (byte) read);
                }
                return decode(in);
            }
        }
    }

    @Override
    protected void saveBuffer() {
        this.message.header = (byte) Protocol.HEADER;
    }
}

