package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;

public class DeliveryParametersRequest extends ReadAction {
    public DeliveryParametersRequest(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_DEMANDE_PARAMETRES_DYNAMIQUES);

        // Inform listener
        listener.deliveryParametersRequest();
    }
}
