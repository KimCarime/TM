package com.lafarge.tm;

public class DeliveryParametersRequest extends MessageType {
    public DeliveryParametersRequest(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_DEMANDE_PARAMETRES_DYNAMIQUES);
        }

        // Inform listener
        if (listener != null) {
            listener.deliveryParametersRequest();
        }
    }
}
