package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;

public class TruckParametersReceived extends ReadAction {
    public TruckParametersReceived(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_PARAMETRES_STATIQUES_RECUS);

        // Inform listener
        listener.truckParametersReceived();
    }
}
