package com.lafarge.tm;

import com.lafarge.tm.utils.Convert;

public class SlumpUpdated extends MessageType {

    public SlumpUpdated(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        listener.slumpUpdated(Convert.buffToInt(data));
    }
}
