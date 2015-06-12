package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.common.Protocol;

public class WaterAdditionRequest extends ReadAction {
    public WaterAdditionRequest(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_DEMANDE_AUTORISATION_AJOUT_EAU);

        // Decode parameters
        int volume = data[0];

        // Inform listener
        listener.waterAdditionRequest(volume);
    }
}
