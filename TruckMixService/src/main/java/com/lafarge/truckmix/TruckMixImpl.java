package com.lafarge.truckmix;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.lafarge.truckmix.bluetooth.ConnectionStateListener;
import com.lafarge.truckmix.common.ManifestChecker;
import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.Communicator;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.EventListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;
import com.lafarge.truckmix.service.TruckMixService;
import com.lafarge.truckmix.service.TruckMixServiceBinder;

public final class TruckMixImpl extends TruckMix {

    // Instances
    private Context mContext;

    // Service communication
    private TruckMixServiceConnector mServiceConnector;

    //
    // Constructor
    //

    protected TruckMixImpl(final Context context) {
        super();

        // Check manifest
        if (!isManifestCheckingDisabled()) {
            ManifestChecker.checkManifestConfiguration(context);
        }

        // Keep reference of the context
        mContext = context.getApplicationContext();

        // Create service connection
        mServiceConnector = new TruckMixServiceConnector(context);
        mServiceConnector.bindToService();
    }

    //
    // Setters
    //

    public void setCommunicatorListener(final CommunicatorListener communicatorListener) {
        super.setCommunicatorListener(communicatorListener);
        if (mServiceConnector.isBound()) {
            mServiceConnector.getServiceBinder().setCommunicatorListener(communicatorListener);
        }
    }

    public void setLoggerListener(final LoggerListener loggerListener) {
        super.setLoggerListener(loggerListener);
        if (mServiceConnector.isBound()) {
            mServiceConnector.getServiceBinder().setLoggerListener(loggerListener);
        }
    }

    public void setEventListener(final EventListener eventListener) {
        super.setEventListener(eventListener);
        if (mServiceConnector.isBound()) {
            mServiceConnector.getServiceBinder().setEventListener(eventListener);
        }
    }

    //
    // API
    //

    @Override
    public void connect(final String address, final ConnectionStateListener connectionStateListenerListener) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            throw new IllegalArgumentException("Address " + address + " isn't a valid address");
        }
        if (mServiceConnector.isBound()) {
            mServiceConnector.getServiceBinder().connect(address, connectionStateListenerListener);
        }
    }

    @Override
    public void disconnect() {
        if (mServiceConnector.isBound()) {
            mServiceConnector.getServiceBinder().disconnect();
        }
    }

    @Override
    public boolean isConnected() {
        if (mServiceConnector.isBound()) {
            return mServiceConnector.getServiceBinder().isConnected();
        } else {
            return false;
        }
    }

    @Override
    public void setTruckParameters(final TruckParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Truck parameters can't be null");
        }
        if (mServiceConnector.isBound()) {
            mServiceConnector.getServiceBinder().setTruckParameters(parameters);
        }
    }

    @Override
    public void deliveryNoteReceived(final DeliveryParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Delivery parameters can't be null");
        }
        if (mServiceConnector.isBound()) {
            mServiceConnector.getServiceBinder().deliveryNoteReceived(parameters);
        }
    }

    @Override
    public void acceptDelivery(final boolean accepted) {
        if (mServiceConnector.isBound()) {
            mServiceConnector.getServiceBinder().acceptDelivery(accepted);
        }
    }

    @Override
    public void endDelivery() {
        if (mServiceConnector.isBound()) {
            mServiceConnector.getServiceBinder().endDelivery();
        }
    }

    @Override
    public void allowWaterAddition(final boolean allowWaterAddition) {
        if (mServiceConnector.isBound()) {
            mServiceConnector.getServiceBinder().allowWaterAddition(allowWaterAddition);
        }
    }

    @Override
    public void changeExternalDisplayState(final boolean activated) {
        if (mServiceConnector.isBound()) {
            mServiceConnector.getServiceBinder().changeExternalDisplayState(activated);
        }
    }

    @Override
    public Communicator.Information getLastInformation() {
        if (mServiceConnector.isBound()) {
            return mServiceConnector.getServiceBinder().getLastInformation();
        } else {
            return null;
        }
    }

    @Override
    protected void setWaterAdditionAllowed(final boolean waterAdditionAllowed) {
        if (mServiceConnector.isBound()) {
            mServiceConnector.getServiceBinder().setWaterRequestAllowed(waterAdditionAllowed);
        }
    }

    @Override
    protected void setQualityTrackingEnabled(final boolean qualityTrackingEnabled) {
        if (mServiceConnector.isBound()) {
            mServiceConnector.getServiceBinder().setQualityTrackingActivated(qualityTrackingEnabled);
        }
    }

    //
    // Internal
    //

    @Override
    protected void shutdown() {
        mServiceConnector.unbindFromService();
    }

    private class TruckMixServiceConnector implements ServiceConnection {
        private Context mmContext;
        private TruckMixServiceBinder mmBinder;
        private boolean mmBound;

        public TruckMixServiceConnector(Context context) {
            mmContext = context;
        }

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "Service connected: " + className.getShortClassName());
            mmBinder = (TruckMixServiceBinder)service;
            mmBound = true;

            // Set params that can be set before service is actually bound to service
            mmBinder.setQualityTrackingActivated(sQualityTrackingEnabled);
            mmBinder.setWaterRequestAllowed(sWaterRequestAllowed);
            mmBinder.setCommunicatorListener(mCommunicatorListener);
            mmBinder.setLoggerListener(mLoggerListener);
            mmBinder.setEventListener(mEventListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "Service disconnected: " + className.getShortClassName());
            mmBinder = null;
            mmBound = false;
        }

        //
        // Connection/Disconnection stuff
        //

        public void bindToService() {
            if (!mmBound) {
                Intent intent = new Intent(mmContext, TruckMixService.class);
                mmContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
            }
        }

        public void unbindFromService() {
            Log.d(TAG, "unbindFromService");
            if (mmBound) {
                Log.d(TAG, "actually unbindService");
                mmContext.unbindService(this);
                mmBinder = null;
            }
        }

        //
        // Getters
        //

        public TruckMixServiceBinder getServiceBinder() {
            return mmBinder;
        }

        public boolean isBound() {
            return mmBound;
        }
    }
}
