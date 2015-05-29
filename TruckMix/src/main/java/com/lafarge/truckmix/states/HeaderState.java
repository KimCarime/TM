package com.lafarge.truckmix.states;

import com.lafarge.truckmix.MessageReceivedListener;
import com.lafarge.truckmix.ProgressListener;
import com.lafarge.truckmix.Protocol;

import java.io.IOException;
import java.io.InputStream;

public final class HeaderState extends State {

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
                progressListener.willProcessByte(ProgressListener.State.STATE_HEADER, (byte) read);
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

