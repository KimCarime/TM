package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.Protocol;

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