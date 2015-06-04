package com.lafarge.truckmix.decoder.states;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.ProgressListener;
import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.utils.Convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class SizeState extends State {

    private static final int SIZE_NB_BYTES = 2;

    private final int type;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

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
            if (progressListener != null) {
                progressListener.willProcessByte(ProgressListener.ProgressState.STATE_SIZE, (byte) read);
            }
            out.write(read);
            if (!isSizeFoundDoesMatchForGivenType(out.toByteArray(), type)) {
                if (progressListener != null) {
                    progressListener.parsingFailed(ProgressListener.ParsingError.ERROR_PARSING_SIZE, (byte) read);
                }
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
