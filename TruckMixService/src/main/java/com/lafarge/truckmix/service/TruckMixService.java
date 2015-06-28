package com.lafarge.truckmix.service;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import com.lafarge.truckmix.BuildConfig;
import com.lafarge.truckmix.TruckMix;
import com.lafarge.truckmix.bluetooth.BluetoothChatService;
import com.lafarge.truckmix.bluetooth.BluetoothChatServiceMessages;
import com.lafarge.truckmix.bluetooth.ConnectionStateListener;
import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.Communicator;
import com.lafarge.truckmix.communicator.listeners.CommunicatorBytesListener;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.EventListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;

import java.lang.ref.WeakReference;
import java.util.Arrays;

public class TruckMixService extends Service implements ITruckMixService {
    private static final String TAG = "TruckMixService";

    // Modules
    private Communicator mCommunicator;
    private BluetoothChatService mBluetoothChat;

    // Client listeners
    // TODO: Those listeners are dedicated to client, we should instead use them directly in the Communicator. So we have to allow the Communicator to have optional listeners.
    private CommunicatorListener mCommunicatorListener;
    private LoggerListener mLoggerListener;
    private EventListener mEventListener;
    private ConnectionStateListener mConnectionStateListener;
    private CommunicatorBytesListener mCommunicatorBytesListener;


    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private final IBinder mBinder = new TruckMixBinder();

    //
    // Service overrides
    //

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "TruckMixService version " + BuildConfig.VERSION_NAME + " is starting up");
    }

    public void start(String address, TruckMix truc) {
        this.mCommunicatorListener = truc.getCommunicatorListener();
        this.mLoggerListener = truc.getLoggerListener();
        this.mEventListener = truc.getEventListener();
        this.mConnectionStateListener = truc.getConnectionStateListener();
        this.mCommunicatorBytesListener = new CommunicatorBytesListener() {
            @Override
            public void send(final byte[] bytes) {
                mBluetoothChat.write(bytes);
            }
        };

        mHandlerThread = new HandlerThread("TruckMixServiceThread");
        mHandlerThread.start();

        mHandler = new Handler(mHandlerThread.getLooper());

        // Module instanciation
        mBluetoothChat = new BluetoothChatService(this, new BluetoothChatServiceHandler(this, mHandlerThread.getLooper()));
        mBluetoothChat.connect(address);
        mCommunicator = new Communicator(mCommunicatorBytesListener, mCommunicatorListener, mLoggerListener, mEventListener);
    }

    @Override
    public void onDestroy() {
        mHandlerThread.quit();
        mBluetoothChat.stop();
//        mBluetoothChat = null;
//        mCommunicator = null;
//        mBinder = null;
//        mClientCommunicatorListener = null;
//        mClientConnectionStateListener = null;
//        mClientEventListener = null;
//        mClientLoggerListener = null;
//        mMainThreadHandler = null;
        Log.i(TAG, "TruckMix stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void setTruckParameters(final TruckParameters parameters) {
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                mCommunicator.setTruckParameters(parameters);
            }});
    }

    @Override
    public void deliveryNoteReceived(final DeliveryParameters parameters) {
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                mCommunicator.deliveryNoteReceived(parameters);
            }});
    }

    @Override
    public void acceptDelivery(final boolean accepted) {
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                mCommunicator.acceptDelivery(accepted);
            }});
    }

    @Override
    public void endDelivery() {
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                mCommunicator.endDelivery();
            }});
    }

    @Override
    public void allowWaterAddition(final boolean allowWaterAddition) {
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                mCommunicator.allowWaterAddition(allowWaterAddition);
            }});
    }

    @Override
    public void changeExternalDisplayState(final boolean activated) {
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                mCommunicator.changeExternalDisplayState(activated);
            }});
    }

    @Override
    public Communicator.Information getLastInformation() {
        return mCommunicator.getLastInformation();
    }

    @Override
    public void setWaterRequestAllowed(final boolean waterRequestAllowed) {
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                mCommunicator.setWaterRequestAllowed(waterRequestAllowed);
            }});
    }

    @Override
    public boolean isWaterRequestAllowed() {
        return mCommunicator.isWaterRequestAllowed();
    }

    @Override
    public void setQualityTrackingActivated(boolean qualityTrackingEnabled) {
        mContext.getCommunicatorInstance().setQualityTrackingActivated(qualityTrackingEnabled);
    }

    @Override
    public boolean isQualityTrackingActivated() {
        return mCommunicator.isQualityTrackingActivated();
    }


    //
    // Private stuff
    //

    /**
     * Handler of incoming bluetooth messages.
     */
    private static class BluetoothChatServiceHandler extends Handler {
        private final WeakReference<TruckMixService> mmService;

        public BluetoothChatServiceHandler(TruckMixService service, Looper looper) {
            super(looper);
            mmService = new WeakReference<TruckMixService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            TruckMixService service = mmService.get();
            if (service == null) {
                return;
            }

            switch (msg.what) {
                case BluetoothChatServiceMessages.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED: {
                            BluetoothChatServiceMessages.BluetoothDeviceInfo deviceInfo = BluetoothChatServiceMessages.getDeviceFromDeviceConnectedMessage(msg);
                            service.mLoggerListener.log("BLUETOOTH: connected to :" + deviceInfo.getName() + " - " + deviceInfo.getAddress());
                            service.mCommunicator.setConnected(true);
                            if (service.mConnectionStateListener != null) {
                                service.mConnectionStateListener.onCalculatorConnected();
                            }
                            break;
                        }
                        case BluetoothChatService.STATE_CONNECTING:
                            BluetoothChatServiceMessages.BluetoothDeviceInfo deviceInfo = BluetoothChatServiceMessages.getDeviceFromDeviceConnectedMessage(msg);
                            service.mLoggerListener.log("BLUETOOTH: connecting to: " + deviceInfo.getName() + " - " + deviceInfo.getAddress());
                            if (service.mConnectionStateListener != null) {
                                service.mConnectionStateListener.onCalculatorConnecting();
                            }
                            break;
                        case BluetoothChatService.STATE_NONE:
                            service.mLoggerListener.log("BLUETOOTH: disconnected");
                            if (service.mCommunicator != null) {
                                service.mCommunicator.setConnected(false);
                            }
                            if (service.mConnectionStateListener != null) {
                                service.mConnectionStateListener.onCalculatorDisconnected();
                            }
                            break;
                    }
                    break;
                case BluetoothChatServiceMessages.MESSAGE_WRITE:
                    break;
                case BluetoothChatServiceMessages.MESSAGE_READ:
                    service.mCommunicator.received(Arrays.copyOf((byte[]) msg.obj, msg.arg1));
                    break;
                case BluetoothChatServiceMessages.MESSAGE_BLUETOOTH_STATE_OFF:
                    if (service.mLoggerListener != null) {
                        service.mLoggerListener.log("BLUETOOTH: bluetooth is off -> turning on");
                    }
                    break;
                case BluetoothChatServiceMessages.MESSAGE_BLUETOOTH_STATE_ON:
                    if (service.mLoggerListener != null) {
                        service.mLoggerListener.log("BLUETOOTH: bluetooth is on");
                    }
                    break;
            }
        }
    }

    public class TruckMixBinder extends Binder {
        public TruckMixService getService() {
            return TruckMixService.this;
        }
    }



    //
    // Inner types
    //

    public interface TruckMixContext {
        //
        // Module access
        //

        TruckMixService getServiceInstance();
        Communicator getCommunicatorInstance();
        BluetoothChatService getBluetoothChatInstance();
    }

    //
    // Private stuff
    //

    private final TruckMixContext mContext = new TruckMixContext() {
        @Override
        public TruckMixService getServiceInstance() {
            return TruckMixService.this;
        }

        @Override
        public Communicator getCommunicatorInstance() {
            return mCommunicator;
        }

        @Override
        public BluetoothChatService getBluetoothChatInstance() {
            return mBluetoothChat;
        }
    };
}
