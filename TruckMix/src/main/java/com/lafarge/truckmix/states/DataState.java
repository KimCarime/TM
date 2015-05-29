package com.lafarge.truckmix.states;

import com.lafarge.truckmix.MessageReceivedListener;
import com.lafarge.truckmix.ProgressListener;
import com.lafarge.truckmix.Protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class DataState extends State {
    private final int type;
    private final int expectedSize;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    public DataState(int type, int size, Message message, MessageReceivedListener messageListener, ProgressListener progressListener) {
        super(message, messageListener, progressListener);
        this.type = type;
        this.expectedSize = size;
    }

    @Override
    public State decode(InputStream in) throws IOException {
        int read = in.read();

        if (read == -1) {
            return this;
        } else {
            if (progressListener != null) {
                progressListener.willProcessByte(ProgressListener.State.STATE_DATA, (byte) read);
            }
            out.write(read);
            if (!isDataFoundValidForGivenType(out.toByteArray(), type)) {
                if (progressListener != null) {
                    progressListener.parsingFailed(ProgressListener.ParsingError.ERROR_PARSING_DATA_BOOLEAN_TYPE, (byte) read);
                }
                return new HeaderState(messageListener, progressListener).decode(in);
            }
            if (out.size() < expectedSize) {
                return decode(in);
            } else {
                saveBuffer();
                return new CrcState(message, messageListener, progressListener).decode(in);
            }
        }
    }

    @Override
    protected void saveBuffer() {
        this.message.data = out.toByteArray();
    }

    private boolean isDataFoundValidForGivenType(byte[] bytesToTest, int typeToMatch) {
        Protocol.Spec spec = getSpec(typeToMatch).getValue();

        if (spec.booleansToCheck != null) {
            for (int offsetToCheck : spec.booleansToCheck) {
                if (offsetToCheck < bytesToTest.length && (bytesToTest[offsetToCheck] != 0x00 && bytesToTest[offsetToCheck] != (byte)0xFF)) {
                    return false;
                }
            }
        }
        return true;
    }
}
