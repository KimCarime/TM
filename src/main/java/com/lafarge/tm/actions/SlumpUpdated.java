package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.Protocol;
import com.lafarge.tm.utils.Convert;

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
        if (listener != null) {
            listener.slumpUpdated(slump);
        }
    }
}
