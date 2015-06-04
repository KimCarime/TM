package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.common.Protocol;

public class TruckParametersRequest extends ReadAction {
    public TruckParametersRequest(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_DEMANDE_PARAMETRES_STATIQUES);
        }

        // Inform listener
        if (listener != null) {
            listener.truckParametersRequest();
        }
    }
}
