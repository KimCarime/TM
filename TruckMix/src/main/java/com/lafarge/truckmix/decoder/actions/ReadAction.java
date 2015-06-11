package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.common.Protocol;

/**
 * Abstract class of a decoded action. This is the final state of the decoder. Subclasses are responsible of decoding
 * data and trigger the corresponding MessageReceivedListener.
 */
public abstract class ReadAction {

    final MessageReceivedListener listener;

    /**
     * Constructs a ReadAction.
     *
     * @param listener The listener that will received the decoded message
     * @throws IllegalArgumentException If listener is equal to null.
     */
    ReadAction(MessageReceivedListener listener) {
        if (listener == null) throw new IllegalArgumentException("listener can't be null");
        this.listener = listener;
    }

    /**
     * Subclass should implement this mode to decode data.
     *
     * @param data A buffer message.
     */
    public abstract void decode(byte[] data);

    /**
     * Throw an exception if data length of a message doesn't match with the given message identifier.
     * Helpful for development but should never occur as the data size is check at parsing time (@see SizeState).
     *
     * @param dataLength The data lengith
     * @param type The message identifier
     * @throws RuntimeException If message identifier doesn't exist or if data length doesn't match with the protocol
     */
    void checkIfDataLengthIsValid(int dataLength, String type) throws RuntimeException {
        Protocol.Spec spec = Protocol.constants.get(type);
        if (spec == null) {
            throw new RuntimeException("The given type `" + type + "`doesn't exist in the protocol");
        }
        if (dataLength != spec.size) {
            throw new RuntimeException("Data length doesn't match with the spec " + type + " of the protocol, given: " + dataLength + ", expected: " + spec.size);
        }
    }

    /**
     * Throw an exception if boolean bytes (in the data part of the message) isn't equal to 0x00 or 0xFF.
     * Helpful for development for should never occur as boolean bytes is check at parsing time (@see DataState).
     *
     * @param b The byte to check
     * @param message The message to display in the exception
     * @throws RuntimeException If checked bytes isn't equal to 0x00 or 0xFF.
     */
    void checkIfBooleanByteIsValid(byte b, String message) {
        if (b != 0x00 && b != (byte)0xFF) {
            throw new RuntimeException(message);
        }
    }

    /**
     * Return bytes in a buffer from start offset to end offset. Useful for data buffers that contains multiple
     * parameters .e.g. CalibrationData.
     *
     * @param datas The data buffers
     * @param start The offset to start the split
     * @param end The offset to end the split
     * @return A new array of bytes
     */
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
