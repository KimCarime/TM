package com.lafarge.truckmix.actions;

import com.lafarge.truckmix.MessageReceivedListener;
import com.lafarge.truckmix.Protocol;

public abstract class ReadAction {

    final MessageReceivedListener listener;

    ReadAction(MessageReceivedListener listener) {
        this.listener = listener;
    }

    public abstract void decode(byte[] data);

    void checkIfDataLengthIsValid(int dataLength, String type) throws RuntimeException {
        Protocol.Spec spec = Protocol.constants.get(type);
        if (spec == null) {
            throw new RuntimeException("The given type `" + type + "`doesn't exist in the protocol");
        }
        if (dataLength != spec.size) {
            throw new RuntimeException("Data length doesn't match with the spec " + type + " of the protocol, given: " + dataLength + ", expected: " + spec.size);
        }
    }

    void checkIfBooleanByteIsValid(byte b, String message) {
        if (b != 0x00 && b != (byte)0xFF) {
            throw new RuntimeException(message);
        }
    }

    byte[] getBytes(byte[] datas, int start, int end) {
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
