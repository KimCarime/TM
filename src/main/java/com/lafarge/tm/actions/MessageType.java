package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessageType {

    protected MessageReceivedListener listener;
    protected Logger logger;

    public MessageType(MessageReceivedListener listener) {
        this.listener = listener;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public abstract void decode(byte[] data);

    protected void checkIfDataLengthIsValid(int dataLength, String type) {
        Protocol.Spec spec = Protocol.constants.get(type);

        if (spec == null) {
            throw new RuntimeException("The given type `" + type + "`doesn't exist in the protocol");
        }

        if (dataLength != spec.size) {
            throw new RuntimeException("Data length doesn't match with the spec " + type + " of the protocol, given: " + dataLength + ", expected: " + spec.size);
        }
    }

    protected void checkIfBooleanByteIsValid(byte b, String message) {
        if (b != 0x00 && b != (byte)0xFF) {
            throw new RuntimeException(message);
        }
    }

    protected byte[] getBytes(byte[] datas, int start, int end) {
        if (start < 0 || datas == null || datas.length <= start || end <= start) {
            return new byte[0];
        }

        int size = (end + 1) - start;
        byte[] result = new byte[size];
        int j = 0;

        for (int i = start; i <= end; i++) {
            result[j++] = datas[i];
        }
        return result;
    }
}
