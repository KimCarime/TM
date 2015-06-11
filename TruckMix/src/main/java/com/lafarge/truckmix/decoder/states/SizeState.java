package com.lafarge.truckmix.decoder.states;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.ProgressListener;
import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.utils.Convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The fourth state of a message. Will return a DataState if we pass valid bytes (i.e. respecting the protocol) or
 * directly a CrcState if size found is equal to 0, otherwise will return HeaderState.
 */
public final class SizeState extends State {

    /** The number of bytes of the size part of a message */
    private static final int SIZE_NB_BYTES = 2;

    private final int type;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    /**
     * Constructs a SizeState
     *
     * @param type The message identifier
     * @see State(Message, MessageReceivedListener, ProgressListener)
     */
    public SizeState(int type, Message message, MessageReceivedListener messageListener, ProgressListener progressListener) {
        super(message, messageListener, progressListener);
        this.type = type;
    }

    @Override
    public State decode(InputStream in) throws IOException {
        int read = in.read();

        if (read == -1) {
            return this;
        } else {
            progressListener.willProcessByte(ProgressListener.ProgressState.STATE_SIZE, (byte) read);
            out.write(read);
            if (!isSizeFoundDoesMatchForGivenType(out.toByteArray(), type)) {
                progressListener.parsingFailed(ProgressListener.ParsingError.ERROR_PARSING_SIZE, (byte) read);
                return new HeaderState(messageListener, progressListener).decode(in);
            }
            if (out.size() < SIZE_NB_BYTES) {
                return decode(in);
            } else {
                saveBuffer();
                int sizeFound = Convert.bytesToInt(out.toByteArray());
                if (sizeFound > 0) {
                    return new DataState(type, sizeFound, message, messageListener, progressListener).decode(in);
                } else {
                    this.message.data = new byte[0];
                    return new CrcState(message, messageListener, progressListener).decode(in);
                }
            }
        }
    }

    @Override
    protected void saveBuffer() {
        byte[] buff = out.toByteArray();
        this.message.sizeMsb = buff[0];
        this.message.sizeLsb = buff[1];
    }

    /** Helper to check if size found match with the protocol bytes by bytes */
    private boolean isSizeFoundDoesMatchForGivenType(byte[] sizeToTest, int type) {
        Protocol.Spec spec = getSpec(type).getValue();
        if (spec.size == Protocol.Spec.SIZE_UNDEFINED) {
            return true;
        }
        byte[] sizeToMatch = Convert.intToBytes(spec.size, 2);
        for (int i = 0; i < sizeToTest.length; i++) {
            if (sizeToTest[i] != sizeToMatch[i]) {
                return false;
            }
        }
        return true;
    }
}
