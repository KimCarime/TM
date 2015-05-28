package com.lafarge.tm;

public interface CommunicatorBytesListener {

    /**
     *  Method triggered when a message should be send
     *
     *  @param bytes The bytes to send
     */
    void send(byte[] bytes);
}
