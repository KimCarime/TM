package com.lafarge.truckmix.service;

import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.Communicator;

public interface ITruckMixService {

    //
    // Bluetooth specific
    //
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
