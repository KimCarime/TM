package com.lafarge.tm;

public class TraceDebug extends MessageType {
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
