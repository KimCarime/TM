package com.lafarge.truckmix.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.lafarge.truckmix.BuildConfig;
import com.lafarge.truckmix.bluetooth.BluetoothChatService;
import com.lafarge.truckmix.bluetooth.BluetoothChatServiceMessages;
import com.lafarge.truckmix.bluetooth.ConnectionStateListener;
import com.lafarge.truckmix.communicator.Communicator;
import com.lafarge.truckmix.communicator.events.Event;
import com.lafarge.truckmix.communicator.listeners.CommunicatorBytesListener;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.EventListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;

import java.lang.ref.WeakReference;
import java.util.Arrays;

public class TruckMixService extends Service {
    private static final String TAG = "TruckMixService";

    // Service
    private static WeakReference<TruckMixService> mService;
    private IBinder mBinder;

    // Modules
    private Communicator mCommunicator;
    private BluetoothChatService mBluetoothChat;

    // Client listeners
    // TODO: Those listeners are dedicated to client, we should instead use them directly in the Communicator. So we have to allow the Communicator to have optional listeners.
    private CommunicatorListener mClientCommunicatorListener;
    private LoggerListener mClientLoggerListener;
    private EventListener mClientEventListener;
    private ConnectionStateListener mClientConnectionStateListener;

    // Other
    private Handler mMainThreadHandler;

    //
    // Service overrides
    //

    @Override
    public void onCreate() {
        Log.i(TAG, "TruckMixService version " + BuildConfig.VERSION_NAME + " is starting up");

        mMainThreadHandler = new Handler(Looper.getMainLooper());

        // Module instanciation
        mBluetoothChat = new BluetoothChatService(this, new BluetoothChatServiceHandler(this));
        mCommunicator = new Communicator(mBytesListener, mCommunicatorListener, mLoggerListener, mEventListener);

        // Create binder
        mBinder = new TruckMixServiceBinder(mContext);
    }

    @Override
    public void onDestroy() {
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

    //
    // Setters
    //

    public void setCommunicatorListener(CommunicatorListener communicatorListener) {
        mClientCommunicatorListener = communicatorListener;
    }

    public void setLoggerListener(LoggerListener loggerListener) {
        mClientLoggerListener = loggerListener;
    }

    public void setEventListener(EventListener eventListener) {
        mClientEventListener = eventListener;
    }

    public void setConnectionStateListener(ConnectionStateListener connectionStateListenerListener) {
        mClientConnectionStateListener = connectionStateListenerListener;
    }

    //
    // Private stuff
    //

    /**
     * Handler of incoming bluetooth messages.
     */
    private static class BluetoothChatServiceHandler extends Handler {
        private final WeakReference<TruckMixService> mmService;

        public BluetoothChatServiceHandler(TruckMixService service) {
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
                            if (service.mClientConnectionStateListener != null) {
                                service.mClientConnectionStateListener.onCalculatorConnected();
                            }
                            break;
                        }
                        case BluetoothChatService.STATE_CONNECTING:
                            BluetoothChatServiceMessages.BluetoothDeviceInfo deviceInfo = BluetoothChatServiceMessages.getDeviceFromDeviceConnectedMessage(msg);
                            service.mLoggerListener.log("BLUETOOTH: connecting to: " + deviceInfo.getName() + " - " + deviceInfo.getAddress());
                            if (service.mClientConnectionStateListener != null) {
                                service.mClientConnectionStateListener.onCalculatorConnecting();
                            }
                            break;
                        case BluetoothChatService.STATE_NONE:
                            service.mLoggerListener.log("BLUETOOTH: disconnected");
                            if (service.mCommunicator != null) {
                                service.mCommunicator.setConnected(false);
                            }
                            if (service.mClientConnectionStateListener != null) {
                                service.mClientConnectionStateListener.onCalculatorDisconnected();
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

    //
    // Communicator listeners
    //

    /**
     * Implementation of the CommunicatorBytesListener.
     * Every bytes retrieved here should be send via Bluetooth.
     */
    private final CommunicatorBytesListener mBytesListener = new CommunicatorBytesListener() {
        @Override
        public void send(final byte[] bytes) {
            mBluetoothChat.write(bytes);
        }
    };

    /**
     * Implementation of the CommunicatorListener.
     */
    private final CommunicatorListener mCommunicatorListener = new CommunicatorListener() {
        @Override
        public void slumpUpdated(final int slump) {
            if (mClientCommunicatorListener != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mClientCommunicatorListener.slumpUpdated(slump);
                    }
                });
            }
        }

        @Override
        public void rotationDirectionChanged(final RotationDirection rotationDirection) {
            if (mClientCommunicatorListener != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mClientCommunicatorListener.rotationDirectionChanged(rotationDirection);
                    }
                });

            }
        }

        @Override
        public void waterAdded(final int volume, final CommunicatorListener.WaterAdditionMode additionMode) {
            if (mClientCommunicatorListener != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mClientCommunicatorListener.waterAdded(volume, additionMode);
                    }
                });
            }
        }

        @Override
        public void waterAdditionRequest(final int volume) {
            if (mClientCommunicatorListener != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mClientCommunicatorListener.waterAdditionRequest(volume);
                    }
                });

            }
        }

        @Override
        public void waterAdditionBegan() {
            if (mClientCommunicatorListener != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mClientCommunicatorListener.waterAdditionBegan();
                    }
                });
            }
        }

        @Override
        public void waterAdditionEnd() {
            if (mClientCommunicatorListener != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mClientCommunicatorListener.waterAdditionEnd();
                    }
                });
            }
        }

        @Override
        public void stateChanged(final int step, final int subStep) {
            if (mClientCommunicatorListener != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mClientCommunicatorListener.stateChanged(step, subStep);
                    }
                });
            }
        }

        @Override
        public void internData(final boolean inputSensorConnected, final boolean outputSensorConnected, final SpeedSensorState speedSensorState) {
            if (mClientCommunicatorListener != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mClientCommunicatorListener.internData(inputSensorConnected, outputSensorConnected, speedSensorState);
                    }
                });
            }
        }

        @Override
        public void calibrationData(final float inputPressure, final float outputPressure, final float rotationSpeed) {
            if (mClientCommunicatorListener != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mClientCommunicatorListener.calibrationData(inputPressure, outputPressure, rotationSpeed);
                    }
                });
            }
        }

        @Override
        public void inputSensorConnectionChanged(final boolean connected) {
            if (mClientCommunicatorListener != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mClientCommunicatorListener.inputSensorConnectionChanged(connected);
                    }
                });
            }
        }

        @Override
        public void outputSensorConnectionChanged(final boolean connected) {
            if (mClientCommunicatorListener != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mClientCommunicatorListener.outputSensorConnectionChanged(connected);
                    }
                });
            }
        }

        @Override
        public void speedSensorStateChanged(final SpeedSensorState speedSensorState) {
            if (mClientCommunicatorListener != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mClientCommunicatorListener.speedSensorStateChanged(speedSensorState);
                    }
                });
            }
        }

        @Override
        public void alarmTriggered(final AlarmType alarmType) {
            if (mClientCommunicatorListener != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mClientCommunicatorListener.alarmTriggered(alarmType);
                    }
                });
            }
        }
    };

    /**
     * Implementation of the LoggerListener
     */
    private final LoggerListener mLoggerListener = new LoggerListener() {
        @Override
        public void log(final String log) {
            if (mClientLoggerListener != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mClientLoggerListener.log(log);
                    }
                });
            }
        }
    };

    /**
     * Implementation of the EventListener
     */
    private final EventListener mEventListener = new EventListener() {
        @Override
        public void onNewEvents(final Event event) {
            if (mClientEventListener != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mClientEventListener.onNewEvents(event);
                    }
                });
            }
        }
    };

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
