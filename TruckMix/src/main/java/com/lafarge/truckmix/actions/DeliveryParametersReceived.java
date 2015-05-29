package com.lafarge.truckmix.actions;

import com.lafarge.truckmix.MessageReceivedListener;
import com.lafarge.truckmix.Protocol;

public class DeliveryParametersReceived extends ReadAction {
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
