package com.lafarge.truckmix;

import android.content.Context;
import android.util.Log;

import com.lafarge.truckmix.bluetooth.ConnectionStateListener;
import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.Communicator;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.EventListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;

/**
 * A class used to set up interaction with the calculator from an <code>Activity</code> or <code>Service</code>.
 * This class is used in conjunction with <code>TruckMixConsumer</code> interface, which provides a callback
 * when the <code>TruckMixService</code> is ready to use. Until this callback is made, no interaction will be possible
 * with the calculator.
 */
public abstract class TruckMix {
    static protected final String TAG = "TruckMix";

    // Singleton
    protected static TruckMix sInstance;

    // Listeners
    protected static CommunicatorListener mCommunicatorListener;
    protected static LoggerListener mLoggerListener;
    protected static EventListener mEventListener;

    // Options
    protected static boolean sWaterRequestAllowed;
    protected static boolean sQualityTrackingEnabled;

    // Others
    private static boolean sTruckMixDisabled = false;
    private static boolean sManifestCheckingDisabled = false;

    //
    // Singleton
    //

    protected TruckMix() {}

    /**
     * An accessor for the singleton instance of this class. A context must be provided, but if
     * you need to use it from a non-Activity or non-Service class, you can attach it to another
     * singleton or a subclass of the Android Application class.
     */
    public static TruckMix getInstance(final Context context) {
        synchronized(TruckMix.class) {
            if (sInstance == null) {
                Log.d(TAG, "TruckMix instance creation");
                sInstance = create(context);
            }
        }
        return sInstance;
    }

    private static TruckMix create(final Context context) {
        if (!sTruckMixDisabled) {
            Log.i(TAG, "TruckMix enable");
            return new TruckMixImpl(context);
        } else {
            Log.i(TAG, "TruckMix disable: shutting down...");
            return new TruckMixNull();
        }
    }

    //
    // Setters
    //

    /**
     * Use this listener to be aware of TruckMix events.
     *
     * @param communicatorListener The implementation of the listener.
     */
    public void setCommunicatorListener(final CommunicatorListener communicatorListener) {
        mCommunicatorListener = communicatorListener;
    }

    /**
     * Use this listener to have logs.
     *
     * @param loggerListener The implementation of the listener.
     */
    public void setLoggerListener(final LoggerListener loggerListener) {
        mLoggerListener = loggerListener;
    }

    /**
     * Use this listener to have logs.
     * Note that if <code>TruckMix#isQualityTrackingActivated()</code> return <code>false</code>
     * then no events will be send.
     *
     * @param eventListener The implementation of the listener.
     * @see TruckMix#setQualityTrackingActivated(boolean)
     */
    public void setEventListener(final EventListener eventListener) {
        mEventListener = eventListener;
    }

    //
    // API
    //

    /**
     * Enable or disable TruckMix. Use with caution because if you disable TruckMix, all feature
     * will be disable.
     *
     * @param context You application package context.
     * @param truckMixDisabled True to disable TruckMix, false to enable it again.
     */
    public static void setTruckMixDisabled(final Context context, final boolean truckMixDisabled) {
        if (sTruckMixDisabled != truckMixDisabled) {
            sTruckMixDisabled = truckMixDisabled;
            sInstance.shutdown();
            sInstance = create(context);
        }
    }

    /**
     * Allows you to retrieve the state of the TruckMix status.
     *
     * @return True if the TruckMix is disabled, otherwise false.
     * @see TruckMix#setTruckMixDisabled(Context, boolean)
     */
    public static boolean isTruckMixDisabled() {
        return sTruckMixDisabled;
    }

    /**
     * Tell the service to try to connect to a remote bluetooth device.
     * If the remote device isn't found, the service will retry to make a connection every 10 sec
     * until it successes.
     *
     * @param address The address of the remote bluetooth device.
     * @param connectionStateListener The state of the connection
     * @throws IllegalArgumentException If address isn't a valid Bluetooth mac-address, such as "00:43:A8:23:10:F0".
     */
    public abstract void connect(final String address, final ConnectionStateListener connectionStateListener);

    /**
     * Disconnect from the calculator
     */
    public abstract void disconnect();

    /**
     * Return the state of the connection of the calculator.
     *
     * @return true if the device is connected to the calculator, false otherwise
     */
    public abstract boolean isConnected();

    /**
     * Set Truck parameters to the service, will be send next time the calculator will request them.
     *
     * @param parameters The truck parameters
     * @throws IllegalArgumentException If parameters is null
     */
    public abstract void setTruckParameters(final TruckParameters parameters);

    /**
     * Set Delivery parameters to the service, will be send next time the calculator will request
     * them.
     *
     * @param parameters The delivery parameters
     * @throws IllegalArgumentException If parameters is null
     */
    public abstract void deliveryNoteReceived(final DeliveryParameters parameters);

    /**
     * Tell the calculator to pass in "delivery in progress" or not, you should not call this
     * method without having called <code>setTruckParameters</code> and
     * <code>deliveryNoteReceived</code> before.
     *
     * @param accepted Pass true to start a delivery or false, to reset the state of the calculator
     */
    public abstract void acceptDelivery(final boolean accepted);

    /**
     * Tell the calculator to end a delivery in progress.
     */
    public abstract void endDelivery();

    /**
     * Tell the calculator to allow or disallow a water addition request, you should have received
     * a request from the calculator before use this method.
     * Also, if you have <code>TruckMix#setWaterRequestAllowed(boolean)</code> to <code>true</code>,
     * this method will have no effect.
     *
     * @param allowWaterAddition true to accept the water addition request, otherwise false.
     */
    public abstract void allowWaterAddition(final boolean allowWaterAddition);

    /**
     * Change the external display state on the truck.
     * Note that if the external display state is for example currently activated, passing true will
     * have no effect on it.
     *
     * @param activated true to activate the external display, or false to shutdown it
     */
    public abstract void changeExternalDisplayState(final boolean activated);

    /**
     * Return last information that was sent by the calculator.
     * If a value has expired, then it will be null.
     * The object is reset each time a new delivery is started.
     *
     * @return The Information.
     * @see com.lafarge.truckmix.communicator.Communicator.Information
     */
    public abstract Communicator.Information getLastInformation();

    //
    // Options
    //

    /**
     * Allow water actions, useful for countries that doesn't allow water addition in the concrete.
     * By default, water request is not allowed.
     *
     * @param waterRequestAllowed true if you want to interact with the water, otherwise false to disable it.
     */
    public static void setWaterRequestAllowed(final boolean waterRequestAllowed) {
        if (sWaterRequestAllowed != waterRequestAllowed) {
            sWaterRequestAllowed = waterRequestAllowed;
            if (sInstance != null) {
                sInstance.setWaterAdditionAllowed(waterRequestAllowed);
            }
        }
    }

    /**
     * Return the state of the water request allowance
     */
    public static boolean isWaterRequestAllowed() {
        return sWaterRequestAllowed;
    }

    /**
     * Activate the quality tracking, if true, events will be send to the EventListener passed
     * in constructor.
     * By default, quality tracking is not enabled.
     *
     * @param qualityTrackingEnabled true to activate the quality tracking, otherwise false to disable it.
     */
    public static void setQualityTrackingActivated(final boolean qualityTrackingEnabled) {
        if (sQualityTrackingEnabled != qualityTrackingEnabled) {
            sQualityTrackingEnabled = qualityTrackingEnabled;
            if (sInstance != null) {
                sInstance.setQualityTrackingEnabled(qualityTrackingEnabled);
            }
        }
    }

    /**
     * Return the state of the quality tracking
     */
    public static boolean isQualityTrackingActivated() {
        return sQualityTrackingEnabled;
    }

    //
    // Utils
    //

    /**
     * Allows disabling check of manifest for proper configuration of service.  Useful for unit
     * testing
     *
     * @param disabled
     */
    public static void setManifestCheckingDisabled(final boolean disabled) {
        sManifestCheckingDisabled = disabled;
    }

    public static boolean isManifestCheckingDisabled() {
        return sManifestCheckingDisabled;
    }

    //
    // Private API
    //

    protected abstract void setWaterAdditionAllowed(final boolean waterAdditionAllowed);
    protected abstract void setQualityTrackingEnabled(final boolean qualityTrackingEnabled);
    protected abstract void shutdown();
}
