package com.lafarge.tm.states;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.ProgressListener;

import java.io.IOException;
import java.io.InputStream;

public final class DataState extends State {
    private final byte[] buffer;
    private int expectedSize = 0;
    private int totalRead = 0;

    public DataState(int size, Message message, MessageReceivedListener messageListener, ProgressListener progressListener) {
        super(message, messageListener, progressListener);
        this.expectedSize = size;
        this.buffer = new byte[size];
    }

    @Override
    public State decode(InputStream in) throws IOException {
        int read = in.read(this.buffer, this.totalRead, this.expectedSize - this.totalRead);

        if (read == -1) {
            logger.info("[DataState] end of buffer -> waiting...");
            return this;
        } else {
            this.totalRead += read;
            if (this.totalRead == this.expectedSize) {
                logger.info("[DataState] did received all bytes -> continue to CrcState", this.totalRead, this.expectedSize);
                saveBuffer();
                return new CrcState(message, messageListener, progressListener).decode(in);
            } else {
                logger.info("[DataState] -> did received {}/{} data bytes -> waiting...", this.totalRead, this.expectedSize);
                return this;
            }
        }
    }

    @Override
    protected void saveBuffer() {
        this.message.data = buffer;
    }
}
