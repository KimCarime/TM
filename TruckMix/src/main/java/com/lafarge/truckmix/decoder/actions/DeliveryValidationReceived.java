package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.common.Protocol;

public class DeliveryValidationReceived extends ReadAction {
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
