package com.lafarge.truckmix.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.lafarge.truckmix.BuildConfig;
import com.lafarge.truckmix.TruckMix;
import com.lafarge.truckmix.bluetooth.BluetoothChatService;
import com.lafarge.truckmix.bluetooth.ConnectionStateListener;
import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.Communicator;
import com.lafarge.truckmix.communicator.listeners.CommunicatorBytesListener;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.EventListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;
import com.lafarge.truckmix.notification.NotificationFactory;

public class TruckMixService extends Service implements ITruckMixService {

    private static final String TAG = "TruckMixService";

    // Modules
    private Communicator mCommunicator;
    private BluetoothChatService mBluetoothChat;

    // Client listeners
    private CommunicatorListener mCommunicatorListener;
    private LoggerListener mLoggerListener;
    private EventListener mEventListener;
    private ConnectionStateListener mConnectionStateListener;

    // Private listeners
    private CommunicatorBytesListener mCommunicatorBytesListener;

    // Thread management
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    // Service
    private final IBinder mBinder = new TruckMixBinder();

    // Options
    private boolean mNotificationActivated = true;
    private PendingIntent mPendingIntent;

    //
    // Inner types
    //

    public class TruckMixBinder extends Binder {
        public TruckMixService getService() {
            return TruckMixService.this;
        }
    }

    public interface TruckMixContext {
        void post(final Runnable runnable);

        TruckMixService getServiceInstance();
        Communicator getCommunicatorInstance();
        BluetoothChatService getBluetoothChatInstance();
    }

    //
    // Service overrides
    //

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "TruckMixService version " + BuildConfig.VERSION_NAME + " is starting up");
        displayNotification(false);
    }

    @Override
    public void onDestroy() {
        mBluetoothChat.stop();
        mHandlerThread.quit();
        stopForeground(true);
        Log.i(TAG, "TruckMixService stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //
    // API
    //

    public void start(String address, TruckMix truc) {
        // Client
        mNotificationActivated = truc.isNotificationActivated();
        mCommunicatorListener = truc.getCommunicatorListener();
        mLoggerListener = truc.getLoggerListener();
        mEventListener = truc.getEventListener();
        mConnectionStateListener = truc.getConnectionStateListener();

        // Thread
        mHandlerThread = new HandlerThread("TruckMixServiceThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        // Internal
        mCommunicatorBytesListener = new CommunicatorBytesListener() {
            @Override
            public void send(final byte[] bytes) {
                mBluetoothChat.write(bytes);
            }
        };

        // Module instantiation
        mCommunicator = new Communicator(mCommunicatorBytesListener, mCommunicatorListener, mLoggerListener, mEventListener);
        mBluetoothChat = new BluetoothChatService(mContext, mConnectionStateListener, mLoggerListener);
        mBluetoothChat.connect(address);
    }

    //
    // ITruckMixService implementation
    //

    @Override
    public boolean isConnected() {
        return mBluetoothChat.getState() == BluetoothChatService.STATE_CONNECTED;
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
    public boolean isExternalDisplayActivated() {
        return mCommunicator.isExternalDisplayStateActivated();
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
    public void setQualityTrackingActivated(final boolean qualityTrackingEnabled) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCommunicator.setQualityTrackingActivated(qualityTrackingEnabled);
            }
        });
    }

    @Override
    public boolean isQualityTrackingActivated() {
        return mCommunicator.isQualityTrackingActivated();
    }

    @Override
    public void setPendingIntent(PendingIntent pendingIntent) {
        mPendingIntent = pendingIntent;
    }

    //
    // Other
    //

    public void displayNotification(boolean connected) {
        if (mNotificationActivated) {
            startForeground(NotificationFactory.NOTIFICATION_TRUCKMIX_ID, NotificationFactory.createTruckMixNotification(this, mPendingIntent, connected));
        }
    }

    //
    // Private stuff
    //

    private final TruckMixContext mContext = new TruckMixContext() {

        @Override
        public void post(final Runnable runnable) {
            mHandler.post(runnable);
        }

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
