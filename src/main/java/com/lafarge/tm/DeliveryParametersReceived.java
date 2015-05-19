package com.lafarge.tm;

public class DeliveryParametersReceived extends MessageType {
    public DeliveryParametersReceived(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_PARAMETRES_DYNAMIQUES_RECUS);
        }

        // Inform listener
        if (listener != null) {
            listener.deliveryParametersReceived();
        }
    }
}
