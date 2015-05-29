package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.Protocol;

public class DeliveryValidationRequest extends ReadAction {
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
