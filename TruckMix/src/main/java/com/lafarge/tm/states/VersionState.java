package com.lafarge.tm.states;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.ProgressListener;
import com.lafarge.tm.Protocol;

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
                progressListener.willProcessByte(ProgressListener.State.STATE_VERSION, (byte) read);
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
