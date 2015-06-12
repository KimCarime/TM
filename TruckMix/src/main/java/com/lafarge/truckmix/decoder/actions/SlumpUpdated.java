package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.utils.Convert;

public class SlumpUpdated extends ReadAction {
    public SlumpUpdated(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_SLUMP_COURANT);

        // Decode parameters
        int slump = Convert.bytesToInt(data);

        // Inform listener
        listener.slumpUpdated(slump);
    }
}
