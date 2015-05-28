package com.lafarge.tm;

import com.lafarge.tm.utils.Convert;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Communicator {
    /**
     *  Internal state of the communicator
     */
    enum State {
        WAITING_FOR_DELIVERY_NOTE,
        WAITING_FOR_DELIVERY_NOTE_ACCEPTATION,
        DELIVERY_IN_PROGRESS
    }

    /**
     *  The period between two send
     */
    public static final long RESET_STATE_IN_MILLIS = 10 * 1000;

    // Encoder/Decoder
    private final GroupEncoder encoder;
    private final Decoder decoder;

    // Listener
    private final CommunicatorBytesListener bytesListener;
    private final CommunicatorListener communicatorListener;
    private final LoggerListener loggerListener;

    // Parameters
    private TruckParameters truckParameters;
    private DeliveryParameters deliveryParameters;

    // Current state
    private State state;

    // Other
    private boolean connected;

    /**
     * Constructor
     *
     * @param communicatorListener
     * @param loggerListener
     */
    public Communicator(CommunicatorBytesListener bytesListener, CommunicatorListener communicatorListener, LoggerListener loggerListener) {
        this.bytesListener = bytesListener;
        this.communicatorListener = communicatorListener;
        this.loggerListener = loggerListener;
        this.encoder = new GroupEncoder(messageSentListener);
        this.decoder = new Decoder(messageReceivedListener, progressListener);
        this.state = State.WAITING_FOR_DELIVERY_NOTE;
        this.connected = true;
    }

    /**
     *  List of actions
     */

    public void setTruckParameters(TruckParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("TruckParameters can't be null");
        }
        if (loggerListener != null) {
            loggerListener.log("ACTION: set truck parameters");
        }
        truckParameters = parameters;
    }

    /**
     *
     * @param parameters
     */
    public void deliveryNoteReceived(DeliveryParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("DeliveryParameters can't be null");
        }
        if (state != State.WAITING_FOR_DELIVERY_NOTE) {
            throw new IllegalStateException("You cannot start a new delivery while the previous one isn't finish");
        }
        if (loggerListener != null) {
            loggerListener.log("ACTION: delivery note received: ");
        }
        deliveryParameters = parameters;
        setState(State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION);
    }

    /**
     *
     * @param accepted
     */
    public void acceptDelivery(boolean accepted) {
        if (state != State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION) {
            throw new IllegalStateException("You should call deliveryNoteReceived() before accepting a delivery");
        }
        if (loggerListener != null) {
            loggerListener.log("ACTION: accept delivery: " + (accepted ? "YES" : "NO"));
        }
        if (accepted) {
            setState(State.DELIVERY_IN_PROGRESS);
        } else {
            setState(State.WAITING_FOR_DELIVERY_NOTE);
        }
    }

    /**
     *
     */
    public void endDelivery() {
        if (state != State.DELIVERY_IN_PROGRESS) {
            throw new IllegalStateException("You can't end a delivery if you have not started one");
        }
        if (loggerListener != null) {
            loggerListener.log("ACTION: end delivery");
        }
        setState(State.WAITING_FOR_DELIVERY_NOTE);
    }

    /**
     *
     */
    public void changeExternalDisplayState(boolean isActivated) {
        if (loggerListener != null) {
            loggerListener.log("ACTION: change external display state: " + (isActivated ? "ACTIVATED" : "NOT ACTIVATED"));
        }
        if (bytesListener != null && isConnected()) {
            bytesListener.send(encoder.changeExternalDisplayState(isActivated));
        }
    }

    /**
     *
     */
    public void allowWaterAddition(boolean isAllowed) {
        if (loggerListener != null) {
            loggerListener.log("ACTION: allow water addition: " + (isAllowed ? "ALLOWED" : "NOT ALLOWED"));
        }
        // TODO: check if there was a water addition request before sending
        if (bytesListener != null && isConnected()) {
            bytesListener.send(encoder.waterAdditionPermission(isAllowed));
        }
    }

    /**
     *
     */
    public void setConnected(boolean isConnected) {

        if (!connected) {
            pauseTimer();
        if (loggerListener != null) {
            loggerListener.log("BLUETOOTH: connection state: " + (isConnected ? "CONNECTED" : "NOT CONNECTED"));
        }
        } else {
            if (state == State.WAITING_FOR_DELIVERY_NOTE) {
                resumeTimer();
            }
        }
        this.connected = isConnected;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     *
     * @param bytes
     */
    public void received(byte[] bytes) {
        try {
            decoder.decode(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    // TODO: should be private
    public void setState(State state) {
        if (loggerListener != null) {
            loggerListener.log("STATE: state changed: " + state.toString());
        }
        pauseTimer();
        switch (state) {
            case WAITING_FOR_DELIVERY_NOTE:
                resumeTimer();
                break;
            case WAITING_FOR_DELIVERY_NOTE_ACCEPTATION:
                }
                break;
            case DELIVERY_IN_PROGRESS:
                cancelTimer();
                if (bytesListener != null && isConnected()) {
                    bytesListener.send(encoder.beginningOfDelivery());
                }
                break;
        }
        this.state = state;
    }

    public State currentState() {
        return state;
    }

    /**
     *
     */
    private Timer timer;
    private boolean timerWasStopped;

    private void resumeTimer() {
        final TimerTask task = new TimerTask() {
            @Override
            public void run () {
                if (bytesListener != null && isConnected()) {
                    bytesListener.send(encoder.endOfDelivery());
                }
            }
        };
        timerWasStopped = false;
        timer = new Timer();
        timer.schedule(task, 0L, RESET_STATE_IN_MILLIS);
    }

    private void pauseTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            timerWasStopped = true;
        }
    }

    /**
     *
     */
    private final MessageReceivedListener messageReceivedListener = new MessageReceivedListener() {
        @Override
        public void slumpUpdated(int slump) {
            if (loggerListener != null) {
                if (state == State.DELIVERY_IN_PROGRESS) {
                    loggerListener.log("RECEIVED: slump updated: " + slump + " mm");
                } else {
                    loggerListener.log("RECEIVED: IGNORED slump updated: " + slump + " mm");
                }
            }
            if (state == State.DELIVERY_IN_PROGRESS) {
                if (communicatorListener != null && isConnected()) {
                    communicatorListener.slumpUpdated(slump);
                }
            }
        }

        @Override
        public void mixingModeActivated() {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: mixing mode activated");
            }
            if (communicatorListener != null && isConnected()) {
                communicatorListener.mixingModeActivated();
            }
        }

        @Override
        public void unloadingModeActivated() {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: unloadingModeActivated");
            }
            if (communicatorListener != null && isConnected()) {
                communicatorListener.unloadingModeActivated();
            }
        }

        @Override
        public void waterAdded(int volume, WaterAdditionMode additionMode) {
            if (loggerListener != null) {
                if (state == State.DELIVERY_IN_PROGRESS) {
                    loggerListener.log("RECEIVED: water added: " + volume + "L, additionMode: " + additionMode.toString());
                } else {
                    loggerListener.log("RECEIVED: IGNORED water added: " + volume + "L, additionMode: " + additionMode.toString());
                }
            }
            if (state == State.DELIVERY_IN_PROGRESS) {
                if (communicatorListener != null && isConnected()) {
                    communicatorListener.waterAdded(volume, additionMode);
                }
            }
        }

        @Override
        public void waterAdditionRequest(int volume) {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: water addition request: " + volume + " L");
            }
            if (state == State.DELIVERY_IN_PROGRESS) {
                if (communicatorListener != null && isConnected()) {
                    communicatorListener.waterAdditionRequest(volume);
                }
            }
        }

        @Override
        public void waterAdditionBegan() {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: water addition began");
            }
            if (communicatorListener != null && isConnected()) {
                communicatorListener.waterAdditionBegan();
            }
        }

        @Override
        public void waterAdditionEnd() {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: water addition end");
            }
            if (communicatorListener != null && isConnected()) {
                communicatorListener.waterAdditionEnd();
            }
        }

        @Override
        public void alarmWaterAdditionBlocked() {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: ALARM: water addition blocked");
            }
            if (communicatorListener != null && isConnected()) {
                communicatorListener.alarmWaterAdditionBlocked();
            }
        }

        @Override
        public void truckParametersRequest() {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: truck parameters request");
            }
            if (bytesListener != null && isConnected()) {
                bytesListener.send(encoder.truckParameters(truckParameters));
            }
        }

        @Override
        public void truckParametersReceived() {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: truck parameters received");
            }
        }

        @Override
        public void deliveryParametersRequest() {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: delivery parameters request");
            }

            // Cancel synchronization on delivery parameters request
            pauseTimer();

            if (state == State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION || state == State.DELIVERY_IN_PROGRESS) {
                if (bytesListener != null && isConnected()) {
                    bytesListener.send(encoder.deliveryParameters(deliveryParameters));
                }
            }
        }

        @Override
        public void deliveryParametersReceived() {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: delivery parameters received");
            }
        }

        @Override
        public void deliveryValidationRequest() {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: delivery validation request");
            }
            if (state == State.DELIVERY_IN_PROGRESS) {
                if (bytesListener != null && isConnected()) {
                    bytesListener.send(encoder.beginningOfDelivery());
                }
            }
        }

        @Override
        public void deliveryValidationReceived() {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: delivery validation received");
            }
        }

        @Override
        public void stateChanged(int step, int subStep) {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: state changed: step=" + step + ", subStep=" + subStep);
            }
            if (communicatorListener != null && isConnected()) {
                communicatorListener.stateChanged(step, subStep);
            }
        }

        @Override
        public void traceDebug(String trace) {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: trace debug: " + trace);
            }
        }

        @Override
        public void rawData(int inPressure, int outPressure, int interval, boolean buttonHold) {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: raw data (inPressure: " + inPressure + ", outPressure:" + outPressure + ", interval: " + interval + ", buttonHold: " + (buttonHold ? "YES" : "NO") + ")");
            }
        }

        @Override
        public void derivedData(RotationDirection rotationDirection, boolean slumpFrameStable, int currentFrameSize, int expectedFrameSize) {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: derived data (rotationDirection: " + rotationDirection.toString() + ", stable: " + (slumpFrameStable ? "YES" : "NO") + ", currentFrameSize: " + currentFrameSize + ", expectedFrameSize: " + expectedFrameSize + ")");
            }
        }

        @Override
        public void internData(boolean inSensorConnected, boolean outSensorConnected, boolean speedTooLow, boolean speedTooHigh, boolean commandEP1Activated, boolean commandVA1Activated) {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: intern data (inSensorConnected: " + (inSensorConnected ? "YES" : "NO") + ", outSensorConnected: " + (outSensorConnected ? "YES" : "NO") + ", speedTooLow: " + (speedTooLow ? "YES" : "NO") + ", speedTooHigh: " + (speedTooHigh ? "YES" : "NO") + ", commandEP1Activated: " + (commandEP1Activated ? "YES" : "NO") + ", commandVA1Activated: " + (commandVA1Activated ? "YES" : "NO") + ")");
            }
        }

        @Override
        public void calibrationData(float inPressure, float outPressure, float rotationSpeed) {
            if (loggerListener != null) {
                if (state == State.DELIVERY_IN_PROGRESS) {
                     loggerListener.log("RECEIVED: calibration data (inPressure: " + inPressure + ", outPressure:" + outPressure + ", rotationSpeed: " + rotationSpeed + ")");
                } else {
                    loggerListener.log("RECEIVED: IGNORED calibration data (inPressure: " + inPressure + ", outPressure:" + outPressure + ", rotationSpeed: " + rotationSpeed + ")");
                }
            }
            if (state == State.DELIVERY_IN_PROGRESS) {
                if (communicatorListener != null && isConnected()) {
                    communicatorListener.calibrationData(inPressure, outPressure, rotationSpeed);
                }
            }
        }

        @Override
        public void alarmWaterMax() {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: ALARM: water max");
            }
            if (communicatorListener != null && isConnected()) {
                communicatorListener.alarmWaterMax();
            }
        }

        @Override
        public void alarmFlowageError() {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: ALARM: flowage error");
            }
            if (communicatorListener != null && isConnected()) {
                communicatorListener.alarmFlowageError();
            }
        }

        @Override
        public void alarmCountingError() {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: ALARM: counting error");
            }
            if (communicatorListener != null && isConnected()) {
                communicatorListener.alarmCountingError();
            }
        }

        @Override
        public void inputSensorConnectionChanged(boolean connected) {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: input sensor connection changed: " + (connected ? "IS CONNECTED" : "NOT CONNECTED"));
            }
            if (communicatorListener != null && isConnected()) {
                communicatorListener.inputSensorConnectionChanged(connected);
            }
        }

        @Override
        public void outputSensorConnectionChanged(boolean connected) {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: output sensor connection changed: " + (connected ? "IS CONNECTED" : "NOT CONNECTED"));
            }
            if (communicatorListener != null && isConnected()) {
                communicatorListener.outputSensorConnectionChanged(connected);
            }
        }

        @Override
        public void speedSensorHasExceedMinThreshold(boolean isOutOfRange) {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: speed sensor has exceed min threshold: " + (isOutOfRange ? "YES" : "NO"));
            }
            if (communicatorListener != null && isConnected()) {
                communicatorListener.speedSensorHasExceedMinThreshold(isOutOfRange);
            }
        }

        @Override
        public void speedSensorHasExceedMaxThreshold(boolean isOutOfRange) {
            if (loggerListener != null) {
                loggerListener.log("RECEIVED: speed sensor has exceed max threshold: " + (isOutOfRange ? "YES" : "NO"));
            }
            if (communicatorListener != null && isConnected()) {
                communicatorListener.speedSensorHasExceedMaxThreshold(isOutOfRange);
            }
        }
    };

    /**
     *
     */
    private final MessageSentListener messageSentListener = new MessageSentListener() {
        @Override
        public void targetSlump(int value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: target slump: " + value + "mm\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void maximumWater(int value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: maximum water: " + value + "L\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void waterAdditionPermission(boolean isAllowed, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: water addition allowed: " + (isAllowed ? "YES" : "NO") + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void changeExternalDisplayState(boolean isActivated, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: change external display state: " + (isActivated ? "ACTIVATED" : "NOT ACTIVATED") + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void endOfDelivery(byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: end of delivery\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void beginningOfDelivery(byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: beginning of delivery\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void loadVolume(double value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: load volume: " + value + " m3" + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void parameterT1(double value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: parameter T1: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void parameterA11(double value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: parameter A11: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void parameterA12(double value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: parameter A12: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void parameterA13(double value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: parameter A13: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void magnetQuantity(int value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: magnet quantity: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void timePump(int value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: time pump: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void timeDelayDriver(int value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: time delay driver: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void pulseNumber(int value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: pulse number: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void flowmeterFrequency(int value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: flowmeter frequency: " + value + "Hz\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void commandPumpMode(TruckParameters.CommandPumpMode commandPumpMode, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: command pump mode: " + commandPumpMode.toString() + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void calibrationInputSensorA(double value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: calibration input sensor A: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void calibrationOutputSensorA(double value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: calibration output sensor A: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void calibrationInputSensorB(double value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: calibration input  sensor B: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void calibrationOutputSensorB(double value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: calibration output sensor B: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void openingTimeEV1(int value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: opening time EV1: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void openingTimeVA1(int value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: opening time VA1: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void countingTolerance(int value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: counting tolerance: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void waitingDurationAfterWaterAddition(int value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: waiting duration after water addition: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void maxDelayBeforeFlowage(int value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: max delay before flowage: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void maxFlowageError(int value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: max flowage error: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void maxCountingError(int value, byte[] bytes) {
            if (loggerListener != null) {
                loggerListener.log("SENT: max couting error: " + value + "\n  " + Convert.bytesToHex(bytes));
            }
        }
    };

    /**
     *
     */
    private final ProgressListener progressListener= new ProgressListener() {
        @Override
        public void timeout() {
            if (loggerListener != null) {
                loggerListener.log("PROCESS: previous state has expired -> reset to HEADER");
            }
        }

        @Override
        public void willDecode(byte[] buff) {
            if (loggerListener != null) {
                loggerListener.log("PROCESS: will decode: " + Convert.bytesToHex(buff));
            }
        }

        @Override
        public void willProcessByte(State state, byte b) {
//            if (loggerListener != null) {
//                loggerListener.log("PROCESS: current state: " + state.toString() + ", will process byte: " + Convert.byteToHex(b));
//            }
        }

        @Override
        public void parsingFailed(ParsingError errorType, byte b) {
            if (loggerListener != null) {
                loggerListener.log("PROCESS: " + errorType.toString() + " for byte: " + Convert.byteToHex(b));
            }
        }
    };
}
