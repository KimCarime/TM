package com.lafarge.truckmix.service;

import com.lafarge.truckmix.bluetooth.ConnectionStateListener;
import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.Communicator;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.EventListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;

public interface ITruckMixService {

    //
    // Client listeners
    //

    void setCommunicatorListener(CommunicatorListener communicatorListener);
    void setLoggerListener(LoggerListener loggerListener);
    void setEventListener(EventListener eventListener);
    void setConnectionStateListener(ConnectionStateListener connectionStateListener);

    //
    // Bluetooth specific
    //

    void connect(final String address);
    void disconnect();
    boolean isConnected();

    //
    // Communicator specific
    //

    void setTruckParameters(final TruckParameters parameters);
    void deliveryNoteReceived(final DeliveryParameters parameters);
    void acceptDelivery(final boolean accepted);
    void endDelivery();
    void allowWaterAddition(final boolean allowWaterAddition);
    void changeExternalDisplayState(final boolean activated);
    Communicator.Information getLastInformation();

    //
    // Options
    //

    void setWaterRequestAllowed(final boolean waterRequestAllowed);
    boolean isWaterRequestAllowed();
    void setQualityTrackingActivated(final boolean qualityTrackingEnabled);
    boolean isQualityTrackingActivated();
}
