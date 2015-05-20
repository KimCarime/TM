package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.Protocol;

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
        if (listener != null) {
            listener.waterAdditionRequest(volume);
        }
    }
}
