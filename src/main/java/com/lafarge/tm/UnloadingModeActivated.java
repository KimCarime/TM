package com.lafarge.tm;

public class UnloadingModeActivated extends MessageType {
    public UnloadingModeActivated(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_PASSAGE_EN_VIDANGE);
        }

        // Inform listener
        if (listener != null) {
            listener.unloadingModeActivated();
        }
    }
}
