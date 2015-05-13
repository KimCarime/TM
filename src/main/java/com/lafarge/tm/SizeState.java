package com.lafarge.tm;

import java.io.IOException;
import java.io.InputStream;

import static com.lafarge.tm.utils.Convert.buffToInt;
import static com.lafarge.tm.utils.Convert.intToBuff;

public final class SizeState extends State {
    public static final int SIZE_NB_BYTES = 2;

    private final int messageType;

    private final byte[] buffer = new byte[SIZE_NB_BYTES];
    private int totalRead = 0;

    public SizeState(int messageType, Message message, MessageReceivedListener messageListener, ProgressListener progressListener) {
        super(message, messageListener, progressListener);
        this.messageType = messageType;
    }

    @Override
    public State decode(InputStream in) throws IOException {
        int read = in.read(this.buffer, this.totalRead, SIZE_NB_BYTES - this.totalRead);

        switch (read) {
            case -1:
                logger.info("[SizeState] end of buffer -> waiting...");
                return this;
            default:
                this.totalRead += read;
                return nextState(in);
        }
    }

    @Override
    protected void saveBuffer() {
        this.message.sizeMsb = this.buffer[0];
        this.message.sizeLsb = this.buffer[1];
    }

    private State nextState(InputStream in) throws IOException {
        State next = null;

        switch (this.totalRead) {
            case 1:
                if (checkIfSizeMatchForGivenMessageType(this.buffer[0], this.messageType)) {
                    logger.info("[SizeState] first byte match with current type's size -> waiting for next byte");
                    next = this;
                } else {
                    logger.warn("[SizeState] the first byte doesn't match with the current type's size");
                }
                break;
            case SIZE_NB_BYTES:
                int sizeFound = buffToInt(this.buffer);

                if (checkIfSizeMatchForGivenMessageType(sizeFound, this.messageType)) {
                    saveBuffer();
                    if (sizeFound > 0) {
                        logger.info("[SizeState] received size match with current type's size -> continue to DataState");
                        next = new DataState(sizeFound, message, messageListener, progressListener).decode(in);
                    } else {
                        logger.info("[SizeState] received size match with current type's size -> continue to CrcState (no need for Data)");
                        next = new CrcState(message, messageListener, progressListener).decode(in);
                    }
                } else {
                    logger.warn("[SizeState] the received size doesn't match with current type");
                }
                break;
            default:
                throw new RuntimeException("The impossible happened: the nb bytes read is not conform to the protocol");
        }
        return (next != null) ? next : new HeaderState(messageListener, progressListener);
    }

    private boolean checkIfSizeMatchForGivenMessageType(byte firstByteToTest, int stateToMatch) {
        Protocol.Pair pair = Protocol.constants.get(stateToMatch);

        if (pair == null) {
            throw new RuntimeException("The impossible happened: The state wasn't recognize");
        }

        byte firstByteToMatch = intToBuff(pair.size)[0];
        logger.debug("[SizeState] received byte: {}, expected byte: {}", String.format("0x%02X", firstByteToTest), String.format("0x%02X", firstByteToMatch));
        return firstByteToTest == firstByteToMatch;
    }

    private boolean checkIfSizeMatchForGivenMessageType(int sizeToTest, int stateToMatch) {
        Protocol.Pair pair = Protocol.constants.get(stateToMatch);

        if (pair == null) {
            throw new RuntimeException("The impossible happened: The state wasn't recognize");
        }

        int sizeToMatch = pair.size;
        logger.debug("[SizeState] received size: {}, expected size: {}", sizeToTest, sizeToMatch);
        return sizeToTest == sizeToMatch;
    }
}