package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.Protocol;

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
