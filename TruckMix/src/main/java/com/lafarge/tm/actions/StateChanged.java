package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.Protocol;

public class StateChanged extends ReadAction {
    public StateChanged(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_FRANCHISSEMENT_TRANSITION);

        // Decode parameters
        int step = data[0];
        int subStep = data[1];

        // Inform listener
        if (listener != null) {
            listener.stateChanged(step, subStep);
        }
    }
}
