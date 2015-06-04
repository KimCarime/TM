package com.lafarge.truckmix.decoder.actions;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;

public class TraceDebug extends ReadAction {
    public TraceDebug(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Decode parameter
        String trace = new String(data);

        // Inform listener
        if (listener != null) {
            listener.traceDebug(trace);
        }
    }
}
