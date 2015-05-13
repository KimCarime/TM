package com.lafarge.tm;

import java.io.IOException;
import java.io.InputStream;

public final class HeaderState extends State {

    public HeaderState(MessageReceivedListener messageListener, ProgressListener progressListener) {
        super(new Message(), messageListener, progressListener);
    }

    @Override
    public State decode(InputStream in) throws IOException {
        int read = in.read();

        switch (read) {
            case -1:
                logger.info("[HeaderState] end of buffer -> waiting...");
                return this;
            case Protocol.HEADER:
                logger.info("[HeaderState] did received header byte -> continue to VersionState");
                saveBuffer();
                return new VersionState(message, messageListener, progressListener).decode(in);
            default:
                logger.warn("[HeaderState] did received incorrect byte -> trying next byte");
                return this.decode(in);
        }
    }

    @Override
    protected void saveBuffer() {
        this.message.header = (byte)Protocol.HEADER;
    }
}

