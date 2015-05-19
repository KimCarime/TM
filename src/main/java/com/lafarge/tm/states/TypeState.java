package com.lafarge.tm.states;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.ProgressListener;
import com.lafarge.tm.Protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.lafarge.tm.utils.Convert.*;

public final class TypeState extends State {
    public static final int TYPE_NB_BYTES = 2;

    private final byte[] buffer = new byte[TYPE_NB_BYTES];
    private int totalRead;

    public TypeState(Message message, MessageReceivedListener messageListener, ProgressListener progressListener) {
        super(message, messageListener, progressListener);
        this.totalRead = 0;
    }

    @Override
    public State decode(InputStream in) throws IOException {
        int read = in.read(this.buffer, this.totalRead, TYPE_NB_BYTES - this.totalRead);

        switch (read) {
            case -1:
                logger.info("[TypeState] end of buffer -> waiting...");
                return this;
            default:
                this.totalRead += read;
                return nextState(in);
        }
    }

    @Override
    protected void saveBuffer() {
        this.message.typeMsb = this.buffer[0];
        this.message.typeLsb = this.buffer[1];
    }

    private State nextState(InputStream in) throws IOException {
        State next = null;

        switch (this.totalRead) {
            case 1:
                if (checkIfFirstByteOfTypeMessageExist(this.buffer[0])) {
                    logger.info("[TypeState] first byte match with a known type -> waiting for next byte");
                    next = this;
                } else {
                    logger.warn("[TypeState] did received incorrect byte -> reset to HeaderState");
                }
                break;
            case TYPE_NB_BYTES:
                int messageTypeFound = buffToInt(this.buffer);

                if (checkIfTypeMessageExist(messageTypeFound)) {
                    logger.info("[TypeState] received message type exist -> continue to SizeState");
                    saveBuffer();
                    next = new SizeState(messageTypeFound, message, messageListener, progressListener).decode(in);
                } else {
                    logger.warn("[TypeState] received type doesn't exit -> reset to HeaderState");
                }
                break;
            default:
                throw new RuntimeException("The impossible happened: the nb bytes read is not conform to the protocol");
        }
        return (next != null) ? next : new HeaderState(messageListener, progressListener);
    }

    private boolean checkIfFirstByteOfTypeMessageExist(byte firstByteToTest) {
        for (Map.Entry<String, Protocol.Spec> entry : Protocol.constants.entrySet()) {
            int messageToMatch = entry.getValue().address;
            byte firstByteToMatch = intToBuff(messageToMatch)[0];

            logger.debug("[TypeState] received byte: {}, expected byte: {}", String.format("0x%02X", firstByteToTest), String.format("0x%02X", firstByteToMatch));
            if (firstByteToTest == firstByteToMatch) {
                return true;
            }
        }
        return false;
    }

    private boolean checkIfTypeMessageExist(int messageTypeToTest) {
        for (Map.Entry<String, Protocol.Spec> entry : Protocol.constants.entrySet()) {
            int typeMessageToMatch = entry.getValue().address;

            logger.debug("[TypeState] received type: {}, expected type: {}", bytesToHex((intToBuff(messageTypeToTest))), bytesToHex((intToBuff(typeMessageToMatch))));
            if (messageTypeToTest == typeMessageToMatch) {
                return true;
            }
        }
        return false;
    }
}
