package com.lafarge.truckmix.states;

import com.lafarge.truckmix.MessageReceivedListener;
import com.lafarge.truckmix.ProgressListener;
import com.lafarge.truckmix.Protocol;
import com.lafarge.truckmix.utils.Convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public final class TypeState extends State {

    private static final int TYPE_NB_BYTES = 2;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    public TypeState(Message message, MessageReceivedListener messageListener, ProgressListener progressListener) {
        super(message, messageListener, progressListener);
    }

    @Override
    public State decode(InputStream in) throws IOException {
        int read = in.read();

        if (read == -1) {
            return this;
        } else {
            if (progressListener != null) {
                progressListener.willProcessByte(ProgressListener.State.STATE_TYPE, (byte) read);
            }
            out.write(read);
            if (!isTypeFoundExist(out.toByteArray())) {
                if (progressListener != null) {
                    progressListener.parsingFailed(ProgressListener.ParsingError.ERROR_PARSING_TYPE, (byte) read);
                }
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
