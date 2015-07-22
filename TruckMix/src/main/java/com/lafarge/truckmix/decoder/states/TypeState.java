package com.lafarge.truckmix.decoder.states;

import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.ProgressListener;
import com.lafarge.truckmix.utils.Convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * The third state of a message. Will return a SizeState if we pass valid bytes (.i.e. if message found exist in the
 * protocol, will return HeaderState.
 */
public final class TypeState extends State {

    /** The nb bytes of Type part of a message */
    private static final int TYPE_NB_BYTES = 2;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    /**
     * Constructs a TypeState
     *
     * @see State(Message, MessageReceivedListener, ProgressListener)
     */
    public TypeState(Message message, MessageReceivedListener messageListener, ProgressListener progressListener) {
        super(message, messageListener, progressListener);
    }

    @Override
    public State decode(InputStream in) throws IOException {
        int read = in.read();

        if (read == -1) {
            return this;
        } else {
            progressListener.willProcessByte(ProgressListener.ProgressState.STATE_TYPE, (byte) read);
            out.write(read);
            if (!isTypeFoundExist(out.toByteArray())) {
                progressListener.parsingFailed(ProgressListener.ParsingError.ERROR_PARSING_TYPE, (byte) read);
                return new HeaderState(messageListener, progressListener).decode(in);
            }
            if (out.size() < TYPE_NB_BYTES) {
                return decode(in);
            } else {
                saveBuffer();
                int typeFound = Convert.bytesToInt(out.toByteArray());
                return new SizeState(typeFound, message, messageListener, progressListener).decode(in);
            }
        }
    }

    @Override
    protected void saveBuffer() {
        byte[] buffer = out.toByteArray();
        this.message.typeMsb = buffer[0];
        this.message.typeLsb = buffer[1];
    }

    /** Helper to check if message found exist in the protocol bytes by bytes */
    private boolean isTypeFoundExist(byte[] typeToTest) {
        for (Map.Entry<String, Protocol.Spec> entry : Protocol.constants.entrySet()) {
            byte[] typeToMatch = Convert.intToBytes(entry.getValue().address, 2);
            boolean isTypeMatching = true;
            for (int i = 0; i < typeToTest.length; i++) {
                if (typeToTest[i] != typeToMatch[i]) {
                    isTypeMatching = false;
                    break;
                }
            }
            if (isTypeMatching) {
                return true;
            }
        }
        return false;
    }
}
