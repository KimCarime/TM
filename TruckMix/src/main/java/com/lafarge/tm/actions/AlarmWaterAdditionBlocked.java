package com.lafarge.tm.actions;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.Protocol;

public class AlarmWaterAdditionBlocked extends ReadAction {
    public AlarmWaterAdditionBlocked(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_NOTIFICATION_AJOUT_EAU_BLOQUE);
        }

        // Inform listener
        if (listener != null) {
            listener.alarmWaterAdditionBlocked();
        }
    }
}
