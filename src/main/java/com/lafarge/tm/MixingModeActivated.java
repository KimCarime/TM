package com.lafarge.tm;

public class MixingModeActivated extends MessageType {
    public MixingModeActivated(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_PASSAGE_EN_MALAXAGE);
        }

        // Inform listener
        if (listener != null) {
            listener.mixingModeActivated();
        }
    }
}
