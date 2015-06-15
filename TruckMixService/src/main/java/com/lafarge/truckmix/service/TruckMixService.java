package com.lafarge.truckmix.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.lafarge.truckmix.communicator.Communicator;
import com.lafarge.truckmix.communicator.events.Event;
import com.lafarge.truckmix.communicator.listeners.CommunicatorBytesListener;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.EventListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.service.bluetooth.BluetoothChatService;
import com.lafarge.truckmix.service.bluetooth.BluetoothChatServiceMessages;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TruckMixService extends Service {
    private static final String TAG = "TruckMixService";

    private Communicator mCommunicator;
    private BluetoothChatService mBluetoothChatService;

    /** Keeps track of all current registered clients. */
    private final List<Messenger> mClients = Collections.synchronizedList(new ArrayList<Messenger>());
    /** Target we publish for clients to sendMessage messages to IncomingHandler */
    private final Messenger mMessenger = new Messenger(new IncomingHandler(this));

    private int mBindCount;

    @Override
    public void onCreate() {
        mBluetoothChatService = new BluetoothChatService(new BluetoothChatServiceHandler(this));
        mCommunicator = new Communicator(mBytesListener, mCommunicatorListener, mLoggerListener, mEventListener);
        showNotification(false);
    }

    @Override
    public void onDestroy() {
        mBluetoothChatService.stop();
        hideNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        ++mBindCount;
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        --mBindCount;
        return super.onUnbind(intent);
    }

    /**
     *
     */
    static abstract class TruckMixServiceHandler extends Handler {
        protected final WeakReference<TruckMixService> mService;

        public TruckMixServiceHandler(TruckMixService service) {
            this.mService = new WeakReference<TruckMixService>(service);
        }
    }

    /**
     * Handler of incoming messages from clients.
     */
    private static class IncomingHandler extends TruckMixServiceHandler {

        public IncomingHandler(TruckMixService service) {
            super(service);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage from client + " + msg.replyTo);

            TruckMixService service = mService.get();
            if (service == null) return;

            switch (msg.what) {
                case TruckMixServiceMessages.MSG_REGISTER_CLIENT:
                    Log.i(TAG, "Client registered: " + msg.replyTo);
                    service.mClients.add(msg.replyTo);
                    break;
                case TruckMixServiceMessages.MSG_UNREGISTER_CLIENT:
                    Log.i(TAG, "Client unregistered: " + msg.replyTo);
                    service.mClients.remove(msg.replyTo);
                    break;
                case TruckMixServiceMessages.MSG_CONNECT_DEVICE:
                    service.mBluetoothChatService.connect(TruckMixServiceMessages.getAddressFromConnectMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_TRUCK_PARAMETERS:
                    service.mCommunicator.setTruckParameters(TruckMixServiceMessages.getDataFromTruckParametersMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_DELIVERY_PARAMETERS:
                    service.mCommunicator.deliveryNoteReceived(TruckMixServiceMessages.getDataFromDeliveryParametersMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_ACCEPT_DELIVERY:
                    service.mCommunicator.acceptDelivery(TruckMixServiceMessages.getValueFromAcceptDeliveryMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_END_DELIVERY:
                    service.mCommunicator.endDelivery();
                    break;
                case TruckMixServiceMessages.MSG_ADD_WATER_PERMISSION:
                    service.mCommunicator.allowWaterAddition(TruckMixServiceMessages.getValueFromAddWaterPermissionMessage(msg));
                    break;
                case TruckMixServiceMessages.MSG_CHANGE_EXTERNAL_DISPLAY_STATE:
                    service.mCommunicator.changeExternalDisplayState(TruckMixServiceMessages
                            .getValueFromChangeExternalDisplayStateMessage(msg));
            }
        }
    };

    /**
     * Handler of incoming bluetooth messages.
     */
    private static class BluetoothChatServiceHandler extends TruckMixServiceHandler {

        public BluetoothChatServiceHandler(TruckMixService service) {
            super(service);
        }

        @Override
        public void handleMessage(Message msg) {
            TruckMixService service = mService.get();
            if (service == null) return;

            switch (msg.what) {
                case BluetoothChatServiceMessages.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            service.mCommunicator.setConnected(true);
                            service.showNotification(true);
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            break;
                        case BluetoothChatService.STATE_NONE:
                            service.mCommunicator.setConnected(false);
                            service.showNotification(false);
                            break;
                    }
                    break;
                case BluetoothChatServiceMessages.MESSAGE_WRITE:
                    break;
                case BluetoothChatServiceMessages.MESSAGE_READ:
                    service.mCommunicator.received(Arrays.copyOf((byte[]) msg.obj, msg.arg1));
                    break;
                case BluetoothChatServiceMessages.MESSAGE_DEVICE_NAME:
                    break;
            }
        }
    }

    /**
     * Implementation of the CommunicatorBytesListener.
     * Every bytes retrieved here should be send via Bluetooth.
     */
    private final CommunicatorBytesListener mBytesListener = new CommunicatorBytesListener() {
        @Override
        public void send(byte[] bytes) {
            mBluetoothChatService.write(bytes);
        }
    };

    /**
     * Implementation of the CommunicatorListener.
     */
    private final CommunicatorListener mCommunicatorListener = new CommunicatorListener() {
        @Override
        public void slumpUpdated(int slump) {
            sendMessage(TruckMixServiceMessages.createSlumpUpdatedMessage(slump));
        }

        @Override
        public void mixingModeActivated() {
            sendMessage(TruckMixServiceMessages.createMixingModeActivatedMessage());
        }

        @Override
        public void unloadingModeActivated() {
            sendMessage(TruckMixServiceMessages.createUnloadingModeActivatedMessage());
        }

        @Override
        public void waterAdded(int volume, MessageReceivedListener.WaterAdditionMode additionMode) {
            sendMessage(TruckMixServiceMessages.createWaterAddedMessage(volume, additionMode));
        }

        @Override
        public void waterAdditionRequest(int volume) {
            sendMessage(TruckMixServiceMessages.createWaterAdditionRequestMessage(volume));
        }

        @Override
        public void waterAdditionBegan() {
        }

        @Override
        public void waterAdditionEnd() {
        }

        @Override
        public void alarmWaterAdditionBlocked() {
            sendMessage(TruckMixServiceMessages.createAlarmWaterAdditionBlockedMessage());
        }

        @Override
        public void stateChanged(int step, int subStep) {
            sendMessage(TruckMixServiceMessages.createStateChangedMessage(step, subStep));
        }

        @Override
        public void calibrationData(float inPressure, float outPressure, float rotationSpeed) {
            sendMessage(TruckMixServiceMessages.createCalibrationDataMessage(inPressure, outPressure, rotationSpeed));
        }

        @Override
        public void alarmWaterMax() {
            sendMessage(TruckMixServiceMessages.createAlarmWaterMaxMessage());
        }

        @Override
        public void alarmFlowageError() {
            sendMessage(TruckMixServiceMessages.createAlarmFlowageErrorMessage());
        }

        @Override
        public void alarmCountingError() {
            sendMessage(TruckMixServiceMessages.createAlarmCountingErrorMessage());
        }

        @Override
        public void inputSensorConnectionChanged(boolean connected) {
            sendMessage(TruckMixServiceMessages.createInputSensorConnectionChangedMessage(connected));
        }

        @Override
        public void outputSensorConnectionChanged(boolean connected) {
            sendMessage(TruckMixServiceMessages.createOutputSensorConnectionChangedMessage(connected));
        }

        @Override
        public void speedSensorHasExceedMinThreshold(boolean thresholdExceed) {
            sendMessage(TruckMixServiceMessages.createSpeedSensorHasExceedMinThresholdMessage(thresholdExceed));
        }

        @Override
        public void speedSensorHasExceedMaxThreshold(boolean thresholdExceed) {
            sendMessage(TruckMixServiceMessages.createSpeedSensorHasExceedMaxThresholdMessage(thresholdExceed));
        }
    };

    /**
     * Implementation of the LoggerListener
     */
    private final LoggerListener mLoggerListener = new LoggerListener() {
        @Override
        public void log(String log) {
            sendMessage(TruckMixServiceMessages.createLogMessage(log));
        }
    };

    /**
     * Implementation of the EventListener
     */
    private final EventListener mEventListener = new EventListener() {
        @Override
        public void onNewEvents(Event event) {

        }
    };

    /**
     * Send a message of the list of clients. Note that we should only have one client in the array.
     */
    private void sendMessage(Message msg) {
        // We are going through the list from back to front so this is safe to do inside the loop.
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Log.i(TAG, "Sending message to clients: " + msg);
                Message m = new Message();
                m.copyFrom(msg);
                mClients.get(i).send(m);
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list
                Log.e(TAG, "Client is dead. Removing from list: " + i);
                mClients.remove(i);
            }
        }
    }

    /**
     *
     */
    static final public int NOTIFICATION_ID = 42;
    private NotificationManager mNotificationManager = null;

    private Notification createNotification(boolean connected) {
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle("TruckMix")
                .setWhen(System.currentTimeMillis())
//                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setTicker("Camion " + (connected ? "connecté" : "déconnecté"))
                .setContentText("Camion " + (connected ? "connecté" : "déconnecté"))
                .build();
    }

    private void hideNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    private void showNotification(boolean connected) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mNotificationManager.cancel(NOTIFICATION_ID);
        mNotificationManager.notify(NOTIFICATION_ID, createNotification(connected));
    }
}
