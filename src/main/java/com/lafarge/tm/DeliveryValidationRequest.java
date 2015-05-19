package com.lafarge.tm;

public class DeliveryValidationRequest extends MessageType {
    public DeliveryValidationRequest(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_DEMANDE_ACCEPTATION_LIVRAISON);
        }

        // Inform listener
        if (listener != null) {
            listener.deliveryValidationRequest();
        }
    }
}
