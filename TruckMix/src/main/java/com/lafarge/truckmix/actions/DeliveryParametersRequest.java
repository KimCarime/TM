package com.lafarge.truckmix.actions;

import com.lafarge.truckmix.MessageReceivedListener;
import com.lafarge.truckmix.Protocol;

public class DeliveryParametersRequest extends ReadAction {
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
