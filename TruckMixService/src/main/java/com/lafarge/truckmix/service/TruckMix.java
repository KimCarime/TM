package com.lafarge.truckmix.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.service.models.DeliveryParametersParcelable;
import com.lafarge.truckmix.service.models.TruckParametersParcelable;

public class TruckMix {

    static private final String TAG = "TruckMix";

    private Context mContext;

    /** Flag indicating whether we have called bind on the service. */
    private boolean mBound;

    /** Messenger for communicating with the service. */
    private Messenger mServiceMessenger = null;

    /** Target we publish for clients to send messages to IncomingHandler. */
    private final Messenger mMessenger;
    private final IncomingHandler mHandler;

    private boolean mSkipWater;

    // Listeners
    private CommunicatorListener communicatorListener;
    private LoggerListener loggerListener;

    public TruckMix(Context context, CommunicatorListener communicatorListener, LoggerListener loggerListener) {
        this.mContext = context;
        this.communicatorListener = communicatorListener;
        this.loggerListener = loggerListener;
        this.mHandler = new IncomingHandler();
        this.mMessenger = new Messenger(this.mHandler);
    }

    public boolean startService() {
        Log.d(TAG, "bind service");
        Intent i = new Intent(mContext.getApplicationContext(), TruckMixService.class);
        return mContext.getApplicationContext().bindService(i, mTruckMixServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void stopService() {
        if (mBound) {
            Log.d(TAG, "unbind service");
            sendMessage(Message.obtain(null, TruckMixServiceMessages.MSG_UNREGISTER_CLIENT));
            mContext.getApplicationContext().unbindService(mTruckMixServiceConnection);
            mBound = false;
        }
    }

    /**
     * Tell the service to try to connect to a remote bluetooth device
     * @param address The address of the remote bluetooth device
     */
    public void connect(String address) {
        sendMessage(TruckMixServiceMessages.createConnectMessage(address));
    }

    /**
     * Set Truck parameters to the service, will be send next time the wirma will request them
     * @param parameters The truck parameters
     * @throws IllegalArgumentException If parameters is null
     */
    public void setTruckParameters(TruckParameters parameters) {
        sendMessage(TruckMixServiceMessages.createTruckParametersMessage(parameters));
    }

    /**
     * Set Delivery parameters to the service, will be send next time the wirma will request them
     * @param parameters The delivery parameters
     * @throws IllegalArgumentException If parameters is null
     */
    public void deliveryNoteReceived(DeliveryParameters parameters) {
        sendMessage(TruckMixServiceMessages.createDeliveryParametersMessage(parameters));
    }

    /**
     * Tell the wirma to pass in "delivery in progress" or not, you should not call this method without having called
     * <code>setTruckParameters</code> and <code>deliveryNoteReceived</code> before.
     * @param accepted Pass true to start a delivery or false, to reset the state of the wirma
     */
    public void acceptDelivery(boolean accepted) {
        sendMessage(TruckMixServiceMessages.createAcceptDeliveryMessage(accepted));
    }

    /**
     * Tell the wirma to end a delivery in progress
     */
    public void endDelivery() {
        sendMessage(TruckMixServiceMessages.createEndDeliveryMessage());
    }

    /**
     * Tell the wirma to allow a water addition or not, you should have received a request from the wirma before use
     * this method.
     * Also, if you have <code>setSkipWater</code> to <code>true</code>, this method will have no effect
     * @param allowWaterAddition
     */
    public void allowWaterAddition(boolean allowWaterAddition) {
        if (mSkipWater) return;
        sendMessage(TruckMixServiceMessages.createAllowWaterAdditionMessage(allowWaterAddition));
    }

    /**
     * Change the external display state on the truck.
     * Note that if the external display state is for example currently activated, passing true will have no effect
     * on it.
     * @param activated true to activate the external display, or false to shutdown it
     */
    public void changeExternalDisplayState(boolean activated) {
        sendMessage(TruckMixServiceMessages.createChangeExternalDisplayState(activated));
    }

    /**
     * Tell the service to skip water request that come from the wirma and the service
     * @param skipWater true to skip water request, or false
     */
    public void setSkipWater(boolean skipWater) {
        this.mSkipWater = skipWater;
    }

    public boolean isSkippingWater() {
        return mSkipWater;
    }

    private void sendMessage(Message msg) {
        if (!mBound || mServiceMessenger == null) return;

        try {
            msg.replyTo = mMessenger;
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            // There is nothing special we need to do if the service has crashed.
            e.printStackTrace();
        }
    }

    /**
     *
     */
     private final ServiceConnection mTruckMixServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Called when the connection with the service is established
            Log.i(TAG, "Service connected: " + className.getShortClassName());
            mServiceMessenger = new Messenger(service);
            mBound = true;
            sendMessage(Message.obtain(null, TruckMixServiceMessages.MSG_REGISTER_CLIENT));
            Toast.makeText(mContext, "Service connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // Called when the connection with the service has been unexpectedly disconnected -- that is, its process
            // crashed.
            Log.i(TAG, "Service disconnected: " + className.getShortClassName());
            mServiceMessenger = null;
            mBound = false;
            Toast.makeText(mContext, "Service disconnected", Toast.LENGTH_SHORT).show();
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
                    loggerListener.log(TruckMixServiceMessages.getLogFromLogMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_SLUMP_UPDATED:
                    communicatorListener.slumpUpdated(TruckMixServiceMessages.getSlumpFromSlumpUpdatedMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_MIXING_MODE_ACTIVATED:
                    communicatorListener.mixingModeActivated();
                    break;
                case TruckMixServiceMessages.MSG_UNLOADING_MODE_ACTIVATED:
                    communicatorListener.unloadingModeActivated();
                    break;
                case TruckMixServiceMessages.MSG_WATER_ADDED: {
                    int volume = TruckMixServiceMessages.getVolumeFromWaterAddedMessage(msg);
                    MessageReceivedListener.WaterAdditionMode additionMode = TruckMixServiceMessages
                            .getAdditionModeFromWaterAddedMessage(msg);
                    communicatorListener.waterAdded(volume, additionMode);
                    break;
                }
                case TruckMixServiceMessages.MSG_WATER_ADDITION_REQUEST:
                    communicatorListener.waterAdditionRequest(TruckMixServiceMessages
                            .getVolumeFromWaterAdditionRequestMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_WATER_ADDITION_BEGAN:
                    communicatorListener.waterAdditionBegan();
                    break;
                case TruckMixServiceMessages.MSG_WATER_ADDITION_END:
                    communicatorListener.waterAdditionEnd();
                    break;
                case TruckMixServiceMessages.MSG_STATE_CHANGED:
                    communicatorListener.stateChanged(msg.arg1, msg.arg2);
                    break;
                case TruckMixServiceMessages.MSG_CALIBRATION_DATA: {
                    float inputPressure = msg.getData().getFloat(TruckMixServiceMessages
                            .KEY_MSG_DATA_CALIBRATION_INPUT_PRESSURE);
                    float outputPressure = msg.getData().getFloat(TruckMixServiceMessages
                            .KEY_MSG_DATA_CALIBRATION_OUTPUT_PRESSION);
                    float rotationSpeed = msg.getData().getFloat(TruckMixServiceMessages
                            .KEY_MSG_DATA_CALIBRATION_ROTATION_SPEED);
                    communicatorListener.calibrationData(inputPressure, outputPressure, rotationSpeed);
                    break;
                }
                case TruckMixServiceMessages.MSG_ALARM_WATER_ADDITION_BLOCK:
                    communicatorListener.alarmWaterAdditionBlocked();
                    break;
                case TruckMixServiceMessages.MSG_ALARM_WATER_MAX:
                    communicatorListener.alarmWaterMax();
                    break;
                case TruckMixServiceMessages.MSG_ALARM_FLOWAGE_ERROR:
                    communicatorListener.alarmFlowageError();
                    break;
                case TruckMixServiceMessages.MSG_ALARM_COUNTING_ERROR:
                    communicatorListener.alarmCountingError();
                    break;
                case TruckMixServiceMessages.MSG_INPUT_SENSOR_CONNECTION_CHANGED:
                    communicatorListener.inputSensorConnectionChanged(TruckMixServiceMessages
                            .getValueFromInputSensorConnectionChangedMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_OUTPUT_SENSOR_CONNECTION_CHANGED:
                    communicatorListener.outputSensorConnectionChanged(TruckMixServiceMessages
                            .getValueFromOutputSensorConnectionChangedMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_SPEED_SENSOR_MIN_EXCEED:
                    communicatorListener.speedSensorHasExceedMinThreshold(TruckMixServiceMessages
                            .getValueFromSpeedSensorHasExceedMinThressThresholdMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_SPEED_SENSOR_MAX_EXCEED:
                    communicatorListener.speedSensorHasExceedMaxThreshold(TruckMixServiceMessages
                            .getValueFromSpeedSensorHasExceedMaxThressThresholdMessage(msg));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
