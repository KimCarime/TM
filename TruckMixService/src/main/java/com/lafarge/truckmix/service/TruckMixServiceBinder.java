package com.lafarge.truckmix.service;

import android.os.Binder;

import com.lafarge.truckmix.bluetooth.ConnectionStateListener;
import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.Communicator;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.EventListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;

/**
 * Class for clients to access.  Because we know this service always
 * runs in the same process as its clients, we don't need to deal with
 * IPC.
 */
public final class TruckMixServiceBinder extends Binder implements ITruckMixService {

    private final TruckMixService.TruckMixContext mContext;

    public TruckMixServiceBinder(final TruckMixService.TruckMixContext context) {
        mContext = context;
    }

    //
    // Listeners
    //

    @Override
    public void setCommunicatorListener(CommunicatorListener communicatorListener) {
        mContext.getServiceInstance().setCommunicatorListener(communicatorListener);
    }

    @Override
    public void setLoggerListener(LoggerListener loggerListener) {
        mContext.getServiceInstance().setLoggerListener(loggerListener);
    }

    @Override
    public void setEventListener(EventListener eventListener) {
        mContext.getServiceInstance().setEventListener(eventListener);
    }

    public void setConnectionStateListener(ConnectionStateListener connectionStateListener) {
        mContext.getServiceInstance().setConnectionStateListener(connectionStateListener);
    }

    //
    // Bluetooth connection specific
    //

    @Override
    public void connect(String address) {
        mContext.getBluetoothChatInstance().connect(address);
    }

    @Override
    public void disconnect() {
        mContext.getBluetoothChatInstance().stop();
    }

    @Override
    public boolean isConnected() {
        return mContext.getBluetoothChatInstance() == null; // TODO
    }

    //
    // Communicator specific
    //

    @Override
    public void setTruckParameters(TruckParameters parameters) {
        mContext.getCommunicatorInstance().setTruckParameters(parameters);
    }

    @Override
    public void deliveryNoteReceived(DeliveryParameters parameters) {
        mContext.getCommunicatorInstance().deliveryNoteReceived(parameters);
    }

    @Override
    public void acceptDelivery(boolean accepted) {
        mContext.getCommunicatorInstance().acceptDelivery(accepted);
    }

    @Override
    public void endDelivery() {
        mContext.getCommunicatorInstance().endDelivery();
    }

    @Override
    public void allowWaterAddition(boolean allowWaterAddition) {
        mContext.getCommunicatorInstance().allowWaterAddition(allowWaterAddition);
    }

    @Override
    public void changeExternalDisplayState(boolean activated) {
        mContext.getCommunicatorInstance().changeExternalDisplayState(activated);
    }

    @Override
    public Communicator.Information getLastInformation() {
        return mContext.getCommunicatorInstance().getLastInformation();
    }

    @Override
    public void setWaterRequestAllowed(boolean waterRequestAllowed) {
        mContext.getCommunicatorInstance().setWaterRequestAllowed(waterRequestAllowed);
    }

    @Override
    public boolean isWaterRequestAllowed() {
        return mContext.getCommunicatorInstance().isWaterRequestAllowed();
    }

    @Override
    public void setQualityTrackingActivated(boolean qualityTrackingEnabled) {
        mContext.getCommunicatorInstance().setQualityTrackingActivated(qualityTrackingEnabled);
    }

    @Override
    public boolean isQualityTrackingActivated() {
        return mContext.getCommunicatorInstance().isQualityTrackingActivated();
    }
}