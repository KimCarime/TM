package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.Protocol;

public class TruckParametersReceived extends ReadAction {
    public TruckParametersReceived(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_PARAMETRES_STATIQUES_RECUS);
        }

        // Inform listener
        if (listener != null) {
            listener.truckParametersReceived();
        }
    }
}
