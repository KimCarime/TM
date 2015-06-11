package com.lafarge.truckmix.communicator.listeners;

/**
 * Interface responsible for bytes to send to the Wirma.
 */
public interface CommunicatorBytesListener {

    /**
     * Method triggered when a message should be send
     *
     * @param bytes The bytes to send
     */
    void send(byte[] bytes);
}
