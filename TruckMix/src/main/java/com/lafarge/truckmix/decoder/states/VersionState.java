package com.lafarge.truckmix.decoder.states;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.ProgressListener;
import com.lafarge.truckmix.common.Protocol;

import java.io.IOException;
import java.io.InputStream;

public final class VersionState extends State {

    public VersionState(Message message, MessageReceivedListener messageListener, ProgressListener progressListener) {
        super(message, messageListener, progressListener);
    }

    @Override
    public State decode(InputStream in) throws IOException {
        int read = in.read();

        if (read == -1) {
            return this;
        } else {
            if (progressListener != null) {
                progressListener.willProcessByte(ProgressListener.ProgressState.STATE_VERSION, (byte) read);
            }
            if (read == Protocol.VERSION) {
                saveBuffer();
                return new TypeState(message, messageListener, progressListener).decode(in);
            } else {
                if (progressListener != null) {
                    progressListener.parsingFailed(ProgressListener.ParsingError.ERROR_PARSING_VERSION, (byte) read);
                }
                return new HeaderState(messageListener, progressListener).decode(in);
            }
        }
    }

    @Override
    protected void saveBuffer() {
        this.message.version = (byte) Protocol.VERSION;
    }
}
