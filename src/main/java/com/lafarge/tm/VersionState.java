package com.lafarge.tm;

import java.io.IOException;
import java.io.InputStream;

public final class VersionState extends State {

    public VersionState(Message message, MessageReceivedListener messageListener, ProgressListener progressListener) {
        super(message, messageListener, progressListener);
    }

    @Override
    public State decode(InputStream in) throws IOException {
        int read = in.read();

        switch (read) {
            case -1:
                logger.info("[VersionState] end of buffer -> waiting...");
                return this;
            case Protocol.VERSION:
                logger.info("[VersionState] did received version byte -> continue to TypeState");
                saveBuffer();
                return new TypeState(message, messageListener, progressListener).decode(in);
            default:
                logger.warn("[VersionState] did received incorrect byte");
                return new HeaderState(messageListener, progressListener);
        }
    }

    @Override
    protected void saveBuffer() {
        this.message.version = (byte) Protocol.VERSION;
    }
}