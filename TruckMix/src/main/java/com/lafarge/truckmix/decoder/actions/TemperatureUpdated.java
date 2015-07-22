package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.utils.Convert;

public class TemperatureUpdated extends ReadAction {

    public TemperatureUpdated(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_TEMPERATURE_COURANTE);

        // Extract parameters
        float temperature = Convert.bytesToFloat(data);

        // Inform listener
        listener.temperatureUpdated(temperature);
    }
}
