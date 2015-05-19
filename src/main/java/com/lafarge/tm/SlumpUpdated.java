package com.lafarge.tm;

import com.lafarge.tm.utils.Convert;

public class SlumpUpdated extends MessageType {
    public SlumpUpdated(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_SLUMP_COURANT);

        // Decode parameters
        int slump = Convert.buffToInt(data);

        // Inform listener
        if (listener != null) {
            listener.slumpUpdated(slump);
        }
    }
}
