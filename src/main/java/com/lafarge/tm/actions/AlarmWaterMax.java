package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.Protocol;

public class AlarmWaterMax extends ReadAction {
    public AlarmWaterMax(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_ERREUR_EAU_MAX);
        }

        // Inform listener
        if (listener != null) {
            listener.alarmWaterMax();
        }
    }
}