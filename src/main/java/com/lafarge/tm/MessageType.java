package com.lafarge.tm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class MessageType {

    protected MessageReceivedListener listener;
    protected Logger logger;

    public MessageType(MessageReceivedListener listener) {
        this.listener = listener;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    protected int shortToInt(byte[] bytes) {
        return 12;
    }

    public abstract void decode(byte[] data);

}
