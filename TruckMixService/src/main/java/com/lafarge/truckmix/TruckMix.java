package com.lafarge.truckmix;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.*;
import android.util.Log;
import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.EventListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.service.TruckMixService;
import com.lafarge.truckmix.service.TruckMixServiceMessages;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A class used to set up interaction with the calculator from an <code>Activity</code> or <code>Service</code>.
 * This class is used in conjunction with <code>TruckMixConsumer</code> interface, which provides a callback
 * when the <code>TruckMixService</code> is ready to use. Until this callback is made, no interaction will be possible
 * with the calculator.
 */
public class TruckMix {
    static private final String TAG = "TruckMix";

    // Instances
    protected static TruckMix mClient;
    private Context mContext;

    // Service communication
    private Messenger mServiceMessenger;
    private boolean mBound;
    private final Messenger mMessenger;
    private final IncomingHandler mHandler;

    // Clients
    private final ConcurrentMap<TruckMixConsumer, ConsumerInfo> mConsumers;

    // Options
    private boolean mWaterRequestAllowed;
    private boolean mQualityTrackingEnabled;

    // Listeners
    private CommunicatorListener mCommunicatorListener;
    private TruckMixConnectionState mConnectionState;
    private LoggerListener mLoggerListener;
    private EventListener mEventListener;

    private static boolean sManifestCheckingDisabled = false;

    /**
     * An accessor for the singleton instance of this class. A context must be provided, but if you need to use it from a non-Activity
     * or non-Service class, you can attach it to another singleton or a subclass of the Android Application class.
     */
    public static TruckMix getInstanceForApplication(Context context) {
        if (mClient == null) {
            Log.d(TAG, "TruckMix instance creation");
            mClient = new TruckMix(context);
        }
        return mClient;
    }

    protected TruckMix(Context context) {
        mContext = context;
        if (!sManifestCheckingDisabled) {
            checkManifest();
        }
        mHandler = new IncomingHandler();
        mMessenger = new Messenger(this.mHandler);
        mConsumers = new ConcurrentHashMap<TruckMixConsumer, ConsumerInfo>();
    }

    /**
     * Binds an Android <code>Activity</code> or <code>Service</code> to the <code>BeaconService</code>. The
     * <code>Activity</code> or <code>Service</code> must implement the <code>beaconConsumer</code> interface so
     * that it can get a callback when the service is ready to use.
     *
     * @param consumer the <code>Activity</code> or <code>Service</code> that will receive the callback when the service is ready.
     */
    public void bind(TruckMixConsumer consumer) {
        synchronized (mConsumers) {
            ConsumerInfo consumerInfo = mConsumers.putIfAbsent(consumer, new ConsumerInfo());
            if (consumerInfo != null) {
                Log.d(TAG, "This consumer is already bound");
            } else {
                Log.d(TAG, "This consumer is not bound. binding: " + consumer);
                Intent intent = new Intent(consumer.getApplicationContext(), TruckMixService.class);
                consumer.bindService(intent, mTruckMixServiceConnection, Context.BIND_AUTO_CREATE);
                Log.d(TAG, "consumer count is now " + mConsumers.size());
            }
        }
    }

    /**
     * Unbinds an Android <code>Activity</code> or <code>Service</code> to the <code>BeaconService</code>.  This should
     * typically be called in the onDestroy() method.
     *
     * @param consumer the <code>Activity</code> or <code>Service</code> that no longer needs to use the service.
     */
    public void unbind(TruckMixConsumer consumer) {
        synchronized (mConsumers) {
            if (mConsumers.containsKey(consumer)) {
                Log.d(TAG, "Unbinding");
                consumer.unbindService(mTruckMixServiceConnection);
                mConsumers.remove(consumer);
                if (mConsumers.size() == 0) {
                    // If this is the last consumer to disconnect, the service will exit
                    // release the serviceMessenger.
                    mServiceMessenger = null;
                }
            } else {
                Log.d(TAG, "This consumer is not bound to: " + consumer);
                Log.d(TAG, "Bound consumers: ");
                Set<Map.Entry<TruckMixConsumer, ConsumerInfo>> consumers = mConsumers.entrySet();
                for (Map.Entry<TruckMixConsumer, ConsumerInfo> consumerEntry : consumers) {
                    Log.d(TAG, String.valueOf(consumerEntry.getValue()));
                }
            }
        }
    }

    /**
     * Tells you if the passed TruckMix consumer is bound to the service.
     *
     * @param consumer The checked consumer
     * @return true if the passed consumer is bound to the service, otherwise false
     */
    public boolean isBound(TruckMixConsumer consumer) {
        synchronized(mConsumers) {
            return consumer != null && mConsumers.get(consumer) != null && mServiceMessenger != null;
        }
    }

    /**
     * Tells you if the any TruckMix consumer is bound to the service.
     *
     * @return true if any consumer is bound, other false
     */
    public boolean isAnyConsumerBound() {
        synchronized(mConsumers) {
            return mConsumers.size() > 0 && mServiceMessenger != null;
        }
    }

    /**
     * Tell the service to try to connect to a remote bluetooth device.
     * If the remote device isn't found, the service will retry to make a connection every 10sec until it successes.
     *
     * @param address The address of the remote bluetooth device
     * @param connectionState
     * @param communicatorListener
     * @param eventListener
     * @throws RemoteException If the TruckMix instance isn't bound to the service.
     * @throws IllegalArgumentException If address isn't a valid mac-address or if mCommunicatorListener is null.
     */
    public void connect(String address, TruckMixConnectionState connectionState, CommunicatorListener communicatorListener, LoggerListener loggerListener, EventListener eventListener) throws RemoteException {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            throw new IllegalArgumentException("Address " + address + " isn't a valid address");
        } else if (connectionState == null || communicatorListener == null || loggerListener == null || eventListener == null) {
            throw new IllegalArgumentException("Parameters can't be null");
        }
        mConnectionState = connectionState;
        mCommunicatorListener = communicatorListener;
        mLoggerListener = loggerListener;
        mEventListener = eventListener;
        sendMessage(TruckMixServiceMessages.createConnectMessage(address));
    }

    /**
     * Set Truck parameters to the service, will be send next time the calculator will request them.
     *
     * @param parameters The truck parameters
     * @throws IllegalArgumentException If parameters is null
     * @throws RemoteException If the TruckMix instance isn't bound to the service.
     */
    public void setTruckParameters(TruckParameters parameters) throws RemoteException {
        if (parameters == null) throw new IllegalArgumentException("Truck parameters can't be null");
        sendMessage(TruckMixServiceMessages.createTruckParametersMessage(parameters));
    }

    /**
     * Set Delivery parameters to the service, will be send next time the calculator will request them.
     *
     * @param parameters The delivery parameters
     * @throws IllegalArgumentException If parameters is null
     * @throws RemoteException If the TruckMix instance isn't bound to the service.
     */
    public void deliveryNoteReceived(DeliveryParameters parameters) throws RemoteException {
        if (parameters == null) throw new IllegalArgumentException("Delivery parameters can't be null");
        sendMessage(TruckMixServiceMessages.createDeliveryParametersMessage(parameters));
    }

    /**
     * Tell the calculator to pass in "delivery in progress" or not, you should not call this method without having called
     * <code>setTruckParameters</code> and <code>deliveryNoteReceived</code> before.
     * 
     * @param accepted Pass true to start a delivery or false, to reset the state of the calculator
     * @throws RemoteException If the TruckMix instance isn't bound to the service.
     */
    public void acceptDelivery(boolean accepted) throws RemoteException {
        sendMessage(TruckMixServiceMessages.createAcceptDeliveryMessage(accepted));
    }

    /**
     * Tell the calculator to end a delivery in progress.
     *
     * @throws RemoteException If the TruckMix instance isn't bound to the service.
     */
    public void endDelivery() throws RemoteException {
        sendMessage(TruckMixServiceMessages.createEndDeliveryMessage());
    }

    /**
     * Tell the calculator to allow a water addition or not, you should have received a request from the calculator before use
     * this method.
     * Also, if you have <code>void setWaterRequestAllowed(boolean)</code> to <code>true</code>, this method will have no effect.
     *
     * @param allowWaterAddition true to accept the water addition request, otherwise false.
     * @throws RemoteException If the TruckMix instance isn't bound to the service.
     */
    public void allowWaterAddition(boolean allowWaterAddition) throws RemoteException {
        if (mWaterRequestAllowed) {
            sendMessage(TruckMixServiceMessages.createAllowWaterAdditionMessage(allowWaterAddition));
        } else {
            Log.w(TAG, "allowWaterAddition ignored because water request isn't allowed - use setWaterRequestAllowed(boolean)");
        }
    }

    /**
     * Change the external display state on the truck.
     * Note that if the external display state is for example currently activated, passing true will have no effect
     * on it.
     *
     * @param activated true to activate the external display, or false to shutdown it
     * @throws RemoteException If the TruckMix instance isn't bound to the service.
     */
    public void changeExternalDisplayState(boolean activated) throws RemoteException {
        sendMessage(TruckMixServiceMessages.createChangeExternalDisplayStateMessage(activated));
    }

    /**
     * Allow water actions, useful for countries that doesn't allow water addition in the concrete.
     * By default, water request is not allowed.
     *
     * @param waterRequestAllowed true if you want to interact with the water, otherwise false to disable it.
     * @throws RemoteException If the TruckMix instance isn't bound to the service.
     */
    public void setWaterRequestAllowed(boolean waterRequestAllowed) throws RemoteException {
        mWaterRequestAllowed = waterRequestAllowed;
        sendMessage(TruckMixServiceMessages.createWaterRequestAllowedMessage(waterRequestAllowed));
    }

    /** Return the state of the water request allowance */
    public boolean isWaterRequestAllowed() {
        return mWaterRequestAllowed;
    }

    /**
     * Activate the quality tracking, if true, events will be send to the EventListener passed in constructor.
     * By default, quality traduction is not enabled.
     *
     * @param qualityTrackingEnabled true to activate the quality tracking, otherwise false to disable it.
     * @throws RemoteException If the TruckMix instance isn't bound to the service.
     */
    public void setQualityTrackingActivated(boolean qualityTrackingEnabled) throws RemoteException {
        mQualityTrackingEnabled = qualityTrackingEnabled;
        sendMessage(TruckMixServiceMessages.createEnableQualityTrackingMessage(qualityTrackingEnabled));
    }

    /** Return the state of the quality tracking */
    public boolean isQualityTrackingActivated() {
        return mQualityTrackingEnabled;
    }

    private void sendMessage(Message msg) throws RemoteException {
        if (!mBound || mServiceMessenger == null) {
            throw new RemoteException("TruckMix is not bound to the service. Call truckMix.bind(TruckMixConsumer consumer) and wait for a callback to onTruckMixServiceConnect()");
        }
        try {
            msg.replyTo = mMessenger;
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            // There is nothing special we need to do if the service has crashed.
            e.printStackTrace();
        }
    }

    /**
     * Implementation of the interface that monitor the state of the connection with the Service.
     */
     private ServiceConnection mTruckMixServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Called when the connection with the service is established
            Log.d(TAG, "Service connected: " + className.getShortClassName());
            mServiceMessenger = new Messenger(service);
            mBound = true;
            synchronized (mConsumers) {
                for (Map.Entry<TruckMixConsumer, ConsumerInfo> entry : mConsumers.entrySet()) {
                    if (!entry.getValue().isConnected) {
                        entry.getKey().onTruckMixServiceConnect();
                        entry.getValue().isConnected = true;
                    }
                }
            }
            try {
                sendMessage(Message.obtain(null, TruckMixServiceMessages.MSG_REGISTER_CLIENT));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // Called when the connection with the service has been unexpectedly disconnected -- that is, its process
            // crashed.
            Log.e(TAG, "Service disconnected: " + className.getShortClassName());
            mServiceMessenger = null;
            mBound = false;
        }
    };

    /**
     * Class for interacting with the main interface of the service.
     */
    public class IncomingHandler extends Handler {
        private static final String TAG = "IncomingHandler";

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TruckMixServiceMessages.MSG_LOG:
                    mLoggerListener.log(TruckMixServiceMessages.getLogFromLogMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_CALCULATOR_CONNECTED:
                    mConnectionState.onCalculatorConnected();
                    break;
                case TruckMixServiceMessages.MSG_CALCULATOR_CONNECTING:
                    mConnectionState.onCalculatorConnecting();
                    break;
                case TruckMixServiceMessages.MSG_CALCULATOR_DISCONNECTED:
                    mConnectionState.onCalculatorDisconnected();
                    break;
                case TruckMixServiceMessages.MSG_SLUMP_UPDATED:
                    mCommunicatorListener.slumpUpdated(TruckMixServiceMessages.getSlumpFromSlumpUpdatedMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_MIXING_MODE_ACTIVATED:
                    mCommunicatorListener.mixingModeActivated();
                    break;
                case TruckMixServiceMessages.MSG_UNLOADING_MODE_ACTIVATED:
                    mCommunicatorListener.unloadingModeActivated();
                    break;
                case TruckMixServiceMessages.MSG_WATER_ADDED: {
                    int volume = TruckMixServiceMessages.getVolumeFromWaterAddedMessage(msg);
                    MessageReceivedListener.WaterAdditionMode additionMode = TruckMixServiceMessages.getAdditionModeFromWaterAddedMessage(msg);
                    mCommunicatorListener.waterAdded(volume, additionMode);
                    break;
                }
                case TruckMixServiceMessages.MSG_WATER_ADDITION_REQUEST:
                    mCommunicatorListener.waterAdditionRequest(TruckMixServiceMessages.getVolumeFromWaterAdditionRequestMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_WATER_ADDITION_BEGAN:
                    mCommunicatorListener.waterAdditionBegan();
                    break;
                case TruckMixServiceMessages.MSG_WATER_ADDITION_END:
                    mCommunicatorListener.waterAdditionEnd();
                    break;
                case TruckMixServiceMessages.MSG_STATE_CHANGED:
                    mCommunicatorListener.stateChanged(msg.arg1, msg.arg2);
                    break;
                case TruckMixServiceMessages.MSG_CALIBRATION_DATA: {
                    float inputPressure = msg.getData().getFloat(TruckMixServiceMessages
                            .KEY_MSG_DATA_CALIBRATION_INPUT_PRESSURE);
                    float outputPressure = msg.getData().getFloat(TruckMixServiceMessages
                            .KEY_MSG_DATA_CALIBRATION_OUTPUT_PRESSION);
                    float rotationSpeed = msg.getData().getFloat(TruckMixServiceMessages
                            .KEY_MSG_DATA_CALIBRATION_ROTATION_SPEED);
                    mCommunicatorListener.calibrationData(inputPressure, outputPressure, rotationSpeed);
                    break;
                }
                case TruckMixServiceMessages.MSG_ALARM_WATER_ADDITION_BLOCK:
                    mCommunicatorListener.alarmWaterAdditionBlocked();
                    break;
                case TruckMixServiceMessages.MSG_ALARM_WATER_MAX:
                    mCommunicatorListener.alarmWaterMax();
                    break;
                case TruckMixServiceMessages.MSG_ALARM_FLOWAGE_ERROR:
                    mCommunicatorListener.alarmFlowageError();
                    break;
                case TruckMixServiceMessages.MSG_ALARM_COUNTING_ERROR:
                    mCommunicatorListener.alarmCountingError();
                    break;
                case TruckMixServiceMessages.MSG_INPUT_SENSOR_CONNECTION_CHANGED:
                    mCommunicatorListener.inputSensorConnectionChanged(TruckMixServiceMessages.getValueFromInputSensorConnectionChangedMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_OUTPUT_SENSOR_CONNECTION_CHANGED:
                    mCommunicatorListener.outputSensorConnectionChanged(TruckMixServiceMessages.getValueFromOutputSensorConnectionChangedMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_SPEED_SENSOR_MIN_EXCEED:
                    mCommunicatorListener.speedSensorHasExceedMinThreshold(TruckMixServiceMessages.getValueFromSpeedSensorHasExceedMinThressThresholdMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_SPEED_SENSOR_MAX_EXCEED:
                    mCommunicatorListener.speedSensorHasExceedMaxThreshold(TruckMixServiceMessages.getValueFromSpeedSensorHasExceedMaxThressThresholdMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_NEW_EVENT:
                    mEventListener.onNewEvents(TruckMixServiceMessages.getDataFromNewEventMessage(msg));
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private class ConsumerInfo {
        public boolean isConnected = false;
    }

    private void checkManifest() {
        final PackageManager packageManager = mContext.getPackageManager();

        // Check service declaration
        final Intent intent = new Intent(mContext, TruckMixService.class);
        List resolveInfo = packageManager.queryIntentServices(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo.size() == 0) {
            throw new ServiceNotDeclaredException();
        }

        // Check permissions
        if (mContext.checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED) {
            throw new RuntimeException("You do not have the android.permission.BLUETOOTH is not properly declared in AndroidManifest.xml");
        }
        if (mContext.checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED) {
            throw new RuntimeException("You do not have the android.permission.BLUETOOTH_ADMIN is not properly declared in AndroidManifest.xml");
        }
    }

    public class ServiceNotDeclaredException extends RuntimeException {
        public ServiceNotDeclaredException() {
            super("The TruckMixService is not properly declared in AndroidManifest.xml.  If using Eclipse," +
                    " please verify that your project.properties has manifestmerger.enabled=true");
        }
    }

    /**
     * Allows disabling check of manifest for proper configuration of service.  Useful for unit
     * testing
     *
     * @param disabled
     */
    public static void setsManifestCheckingDisabled(boolean disabled) {
        sManifestCheckingDisabled = disabled;
    }
}
