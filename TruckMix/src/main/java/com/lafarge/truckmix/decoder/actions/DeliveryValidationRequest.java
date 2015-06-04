package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.common.Protocol;

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
