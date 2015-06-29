package com.lafarge.truckmix;

import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.Communicator;

public final class TruckMixNull extends TruckMix {

    protected TruckMixNull() {
        super();
    }

    @Override
    public void connect(final String address) {}

    @Override
    public void disconnect() {}

    @Override
    public boolean isConnected() { return false; }

    @Override
    public void setTruckParameters(final TruckParameters parameters) {}

    @Override
    public void deliveryNoteReceived(final DeliveryParameters parameters) {}

    @Override
    public void acceptDelivery(final boolean accepted) {}

    @Override
    public void endDelivery() {}

    @Override
    public void allowWaterAddition(final boolean allowWaterAddition) {}

    @Override
    public void changeExternalDisplayState(final boolean activated) {}

    @Override
    public Communicator.Information getLastInformation() { return null; }

    @Override
    protected void setWaterAdditionAllowed(final boolean waterAdditionAllowed) {}

    @Override
    protected void setQualityTrackingEnabled(final boolean qualityTrackingEnabled) {}

    @Override
    protected void shutdown() {}
}
