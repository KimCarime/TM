package com.lafarge.truckmix.communicator.listeners;

/**
 * Interface responsible of sending logs of the Communicator.
 */
public interface LoggerListener {
    /**
     * Triggered when the Communicator send logs.
     *
     * @param log The message
     */
    void log(String log);
}
