package com.lafarge.tm;

public class ErrorCounting extends MessageType {
    public ErrorCounting(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_ERREUR_COMPTAGE);
        }

        // Inform listener
        if (listener != null) {
            listener.countingError();
        }
    }
}
