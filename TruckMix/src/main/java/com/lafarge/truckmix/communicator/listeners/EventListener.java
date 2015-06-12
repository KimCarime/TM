package com.lafarge.truckmix.communicator.listeners;

import com.lafarge.truckmix.communicator.events.Event;

/**
 * Interface of events received from the Wirma.
 */
public interface EventListener {
    void onNewEvents(Event event);
}