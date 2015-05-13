package com.lafarge.tm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.lafarge.tm.utils.Convert.bytesToHex;

public class Decoder {

    public static final int STATE_EXPIRATION_DELAY_MILLIS = 1000;

    private static final Logger logger = LoggerFactory.getLogger(Decoder.class);

    private MessageReceivedListener messageListener;
    private ProgressListener progressListener;

    private State state;
    private long lastDecode;

    public Decoder(MessageReceivedListener messageListener, ProgressListener progressListener) {
        this.state = new HeaderState(messageListener, progressListener);
        this.lastDecode = -1;
    }

    public void decode(byte[] in) throws IOException {
        logger.info("will decode: {}", bytesToHex(in));
        boolean expired = this.lastDecode > 0 && (System.currentTimeMillis() - this.lastDecode > STATE_EXPIRATION_DELAY_MILLIS);
        this.lastDecode = System.currentTimeMillis();
        if (expired && !(state instanceof HeaderState)) {
            logger.info("previous state has expired -> reset to HeaderState");
            this.state = new HeaderState(messageListener, progressListener).decode(new ByteArrayInputStream(in));
        } else {
            this.state = this.state.decode(new ByteArrayInputStream(in));
        }
    }
}
