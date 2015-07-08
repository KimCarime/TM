package com.lafarge.truckmix;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.lafarge.truckmix.bluetooth.ConnectionStateListener;
import com.lafarge.truckmix.common.enums.AlarmType;
import com.lafarge.truckmix.common.enums.RotationDirection;
import com.lafarge.truckmix.common.enums.SpeedSensorState;
import com.lafarge.truckmix.common.enums.WaterAdditionMode;
import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.Communicator;
import com.lafarge.truckmix.communicator.events.Event;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.EventListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;
import com.lafarge.truckmix.service.ITruckMixService;
import com.lafarge.truckmix.service.TruckMixService;

public class TruckMix implements ITruckMixService {
    // Service
    private Context mContext;
    private TruckMixService mBoundService;
    private boolean mIsBound;

    // Listeners
    private CommunicatorListener mCommunicatorListener;
    private LoggerListener mLoggerListener;
    private EventListener mEventListener;
    private ConnectionStateListener mConnectionStateListener;

    // Properties stored in case of service not bounds
    private TruckParameters mTruckParameters;
    private DeliveryParameters mDeliveryParameters;
    private boolean mChangeExternalDisplayState;
    private boolean mWaterRequestAllowed;
    private boolean mQualityTrackingEnabled;
    private boolean mAcceptDelivery;

    // Api
    private String mAddress;

    // Others
    private boolean mManifestCheckingDisabled = false;

    //
    // Constructor
    //

    private TruckMix(
            Context context,
            CommunicatorListener communicatorListener,
            EventListener eventListener,
            LoggerListener loggerListener,
            ConnectionStateListener connectionStateListener) {
        mContext = context;
        mCommunicatorListener = communicatorListener;
        mEventListener = eventListener;
        mLoggerListener = loggerListener;
        mConnectionStateListener = connectionStateListener;
    }

    //
    // API
    //

    public void start(final String address) {
        mAddress = address;
        mContext.bindService(new Intent(mContext, TruckMixService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    public void stop() {
        if (mIsBound) {
            mContext.unbindService(mConnection);
            mIsBound = false;
            mBoundService = null;
        }
    }

    @Override
    public boolean isConnected() {
        return mIsBound && mBoundService.isConnected();
    }

    @Override
    public void setTruckParameters(TruckParameters parameters) {
        mTruckParameters = parameters;
        if (mIsBound) {
            mBoundService.setTruckParameters(parameters);
        }
    }

    @Override
    public void deliveryNoteReceived(DeliveryParameters parameters) {
        mDeliveryParameters = parameters;
        if (mIsBound) {
            mBoundService.deliveryNoteReceived(parameters);
        }
    }

    @Override
    public void acceptDelivery(boolean accepted) {
        mAcceptDelivery = accepted;
        if (mIsBound) {
            mBoundService.acceptDelivery(accepted);
        }
    }

    @Override
    public void endDelivery() {
        if (mIsBound) {
            mBoundService.endDelivery();
        }
    }

    @Override
    public void allowWaterAddition(boolean allowWaterAddition) {
        if (mIsBound) {
            mBoundService.allowWaterAddition(allowWaterAddition);
        }
    }

    @Override
    public void changeExternalDisplayState(boolean activated) {
        mChangeExternalDisplayState = activated;
        if (mIsBound) {
            mBoundService.changeExternalDisplayState(activated);
        }
    }

    @Override
    public Communicator.Information getLastInformation() {
        return mIsBound ? mBoundService.getLastInformation() : null;
    }

    @Override
    public void setWaterRequestAllowed(boolean waterRequestAllowed) {
        mWaterRequestAllowed = waterRequestAllowed;
        if (mIsBound) {
            mBoundService.setWaterRequestAllowed(waterRequestAllowed);
        }
    }

    @Override
    public boolean isWaterRequestAllowed() {
        return mIsBound && mBoundService.isWaterRequestAllowed();
    }

    @Override
    public void setQualityTrackingActivated(boolean qualityTrackingEnabled) {
        mQualityTrackingEnabled = qualityTrackingEnabled;
        if (mIsBound) {
            mBoundService.setQualityTrackingActivated(qualityTrackingEnabled);
        }
    }

    @Override
    public boolean isQualityTrackingActivated() {
        return mIsBound && mBoundService.isQualityTrackingActivated();
    }

    //
    // Getters
    //

    public CommunicatorListener getCommunicatorListener() {
        return mCommunicatorListener;
    }

    public LoggerListener getLoggerListener() {
        return mLoggerListener;
    }

    public EventListener getEventListener() {
        return mEventListener;
    }

    public ConnectionStateListener getConnectionStateListener() {
        return mConnectionStateListener;
    }

    //
    // Service connection management
    //

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((TruckMixService.TruckMixBinder)service).getService();
            mBoundService.start(mAddress, TruckMix.this);
            mIsBound = true;

            if (mTruckParameters != null) {
                setTruckParameters(mTruckParameters);
            }
            if (mDeliveryParameters != null) {
                deliveryNoteReceived(mDeliveryParameters);
            }
            setWaterRequestAllowed(mWaterRequestAllowed);
            setQualityTrackingActivated(mQualityTrackingEnabled);
            if (mAcceptDelivery) {
                acceptDelivery(true);
            }
            changeExternalDisplayState(mChangeExternalDisplayState);
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            mIsBound = false;
        }
    };

    /**
     * Builder for creating a TruckMix instance with its options.
     */
    public static class Builder {
        private final Context mmContext;
        private CommunicatorListener mmCommunicatorListener;
        private LoggerListener mmLoggerListener;
        private EventListener mmEventListener;
        private ConnectionStateListener mmConnectionStateListener;

        public Builder(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null.");
            }
            mmContext = context.getApplicationContext();
        }

        public Builder setCommunicatorListener(CommunicatorListener communicatorListener) {
            mmCommunicatorListener = communicatorListener;
            return this;
        }

        public Builder setLoggerListener(LoggerListener loggerListener) {
            mmLoggerListener = loggerListener;
            return this;
        }

        public Builder setEventListener(EventListener eventListener) {
            mmEventListener = eventListener;
            return this;
        }

        public Builder setConnectionStateListener(ConnectionStateListener connectionStateListener) {
            mmConnectionStateListener = connectionStateListener;
            return this;
        }

        public TruckMix build() {
            if (mmCommunicatorListener == null) {
                mmCommunicatorListener = new CommunicatorListener() {
                    @Override
                    public void slumpUpdated(int slump) {}

                    @Override
                    public void temperatureUpdated(float temperature) {}

                    @Override
                    public void rotationDirectionChanged(RotationDirection rotationDirection) {}

                    @Override
                    public void waterAdded(int volume, WaterAdditionMode additionMode) {}

                    @Override
                    public void waterAdditionRequest(int volume) {}

                    @Override
                    public void waterAdditionBegan() {}

                    @Override
                    public void waterAdditionEnd() {}

                    @Override
                    public void stateChanged(int step, int subStep) {}

                    @Override
                    public void internData(boolean inputSensorConnected, boolean outputSensorConnected, SpeedSensorState speedSensorState) {}

                    @Override
                    public void calibrationData(float inputPressure, float outputPressure, float rotationSpeed) {}

                    @Override
                    public void inputSensorConnectionChanged(boolean connected) {}

                    @Override
                    public void outputSensorConnectionChanged(boolean connected) {}

                    @Override
                    public void speedSensorStateChanged(SpeedSensorState speedSensorState) {}

                    @Override
                    public void alarmTriggered(AlarmType alarmType) {}
                };
            }
            if (mmLoggerListener == null) {
                mmLoggerListener = new LoggerListener() {
                    @Override
                    public void log(String log) {
                    }
                };
            }
            if (mmConnectionStateListener == null) {
                mmConnectionStateListener = new ConnectionStateListener() {
                    @Override
                    public void onCalculatorConnected() {}

                    @Override
                    public void onCalculatorConnecting() {}

                    @Override
                    public void onCalculatorDisconnected() {}
                };
            }
            if (mmEventListener == null) {
                mmEventListener = new EventListener() {
                    @Override
                    public void onNewEvents(Event event) {}
                };
            }
            if (mmConnectionStateListener == null) {
                mmConnectionStateListener = new ConnectionStateListener() {
                    @Override
                    public void onCalculatorConnected() {}

                    @Override
                    public void onCalculatorConnecting() {}

                    @Override
                    public void onCalculatorDisconnected() {}
                };
            }
            return new TruckMix(mmContext, mmCommunicatorListener, mmEventListener, mmLoggerListener, mmConnectionStateListener);
        }
    }
}
