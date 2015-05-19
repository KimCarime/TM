package com.lafarge.tm;

public class WaterAdditionEnd extends MessageType {
    public WaterAdditionEnd(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_FIN_AJOUT_EAU);
        }

        // Inform listener
        if (listener != null) {
            listener.waterAdditionEnd();
        }
    }
}
