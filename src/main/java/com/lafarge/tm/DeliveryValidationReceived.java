package com.lafarge.tm;

public class DeliveryValidationReceived extends MessageType {
    public DeliveryValidationReceived(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_ACCEPTATION_LIVRAISON_RECUE);
        }

        // Inform listener
        if (listener != null) {
            listener.deliveryValidationReceived();
        }
    }
}
