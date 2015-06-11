package com.lafarge.truckmix.communicator;

import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.listeners.CommunicatorBytesListener;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;
import com.lafarge.truckmix.decoder.Decoder;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.ProgressListener;
import com.lafarge.truckmix.encoder.GroupEncoder;
import com.lafarge.truckmix.encoder.listeners.MessageSentListener;
import com.lafarge.truckmix.utils.Convert;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class is the entry point of the whole library, it is responsible of the communication between the client
 * and the Wirma. All bytes to send to the Wirma will be given here (through listener) and all bytes received will be
 * process here, and events will be triggered here.
 */
public class Communicator {
    /**
     * Internal state of the communicator
     */
    enum State {
        WAITING_FOR_DELIVERY_NOTE,
        WAITING_FOR_DELIVERY_NOTE_ACCEPTATION,
        DELIVERY_IN_PROGRESS
    }

    /**
     * Interval between two send of "end of delivery" in order to keep the Wirma isSync with us
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
    private boolean isSync;
    private boolean isConnected;

    // Other
    private Timer timer;

    /**
     * Constructor should be called only once per session
     *
     * @param bytesListener Consumer should implement this listener to know what bytes to send
     * @param communicatorListener Listener of functional events e.g. slump updates or sensor's state changes
     * @param loggerListener Listener of logs, consumer will have logs about what was decoded, encoded, and errors
     *                       while parsing.
     * @throws IllegalArgumentException If one of the parameters is null
     */
    public Communicator(CommunicatorBytesListener bytesListener, CommunicatorListener communicatorListener, LoggerListener loggerListener) {
        if (bytesListener == null) throw new IllegalArgumentException("bytes listener can't be null");
        if (communicatorListener == null) throw new IllegalArgumentException("communicatorListener can't be null");
        if (loggerListener == null) throw new IllegalArgumentException("loggerListener can't be null");

        this.bytesListener = bytesListener;
        this.communicatorListener = communicatorListener;
        this.loggerListener = loggerListener;
        this.encoder = new GroupEncoder(messageSentListener);
        this.decoder = new Decoder(messageReceivedListener, progressListener);
        this.state = State.WAITING_FOR_DELIVERY_NOTE;
        this.isConnected = true;
    }

    //
    // List of actions
    //

    /**
     * Set the truck parameters. Will be send next time the Wirma request them.
     *
     * @param parameters The truck parameters
     * @throws IllegalArgumentException If parameters is null
     */
    public void setTruckParameters(TruckParameters parameters) {
        if (parameters == null) throw new IllegalArgumentException("TruckParameters can't be null");
        loggerListener.log("ACTION: set truck parameters:\n    " + parameters.toString());
        truckParameters = parameters;
    }

    /**
     * Set the delivery parameters. Will be send next time the Wirma request them.
     *
     * @param parameters The delivery parameters
     * @throws IllegalArgumentException If parameters is null
     */
    public void deliveryNoteReceived(DeliveryParameters parameters) {
        if (currentState() != State.WAITING_FOR_DELIVERY_NOTE) {
            throw new IllegalStateException("You cannot start a new delivery while the previous one isn't finish");
        }
        if (parameters == null) throw new IllegalArgumentException("DeliveryParameters can't be null");
        loggerListener.log("ACTION: delivery note received:\n    " + parameters.toString());
        deliveryParameters = parameters;
        setState(State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION);
    }

    /**
     * Tell to the Wirma that we accepted or not the delivery. Note that you should call this method only after having
     * given delivery parameters.
     *
     * @param accepted true to tell the Wirma to start a delivery, otherwise no.
     */
    public void acceptDelivery(boolean accepted) {
//        if (currentState() != State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION) {
//            throw new IllegalStateException("You should call deliveryNoteReceived() before accepting a delivery");
//        }
        loggerListener.log("ACTION: accept delivery: " + (accepted ? "YES" : "NO"));
        setState(accepted ? State.DELIVERY_IN_PROGRESS : State.WAITING_FOR_DELIVERY_NOTE);
    }

    /**
     * Tell the Wirma to end the current delivery in progress.
     */
    public void endDelivery() {
        if (currentState() != State.DELIVERY_IN_PROGRESS) {
            throw new IllegalStateException("You can't end a delivery if you have not started one");
        }
        loggerListener.log("ACTION: end delivery");
        setState(State.WAITING_FOR_DELIVERY_NOTE);
    }

    /**
     * Change the external display state of the truck.
     *
     * @param isActivated true to activate it, otherwise false to deactivate it.
     */
    public void changeExternalDisplayState(boolean isActivated) {
        loggerListener.log("ACTION: change external display state: " + (isActivated ? "ACTIVATED" : "NOT ACTIVATED"));
        if (isConnected) {
            bytesListener.send(encoder.fake());
            bytesListener.send(encoder.changeExternalDisplayState(isActivated));
        }
    }

    /**
     * Give Wirma the permission to add water or note after. You should call this method only if you received a request
     * to add water.
     *
     * @param isAllowed
     */
    public void allowWaterAddition(boolean isAllowed) {
        loggerListener.log("ACTION: allow water addition: " + (isAllowed ? "ALLOWED" : "NOT ALLOWED"));
        // TODO: check if there was a water addition request before sending
        if (isConnected) {
            bytesListener.send(encoder.fake());
            bytesListener.send(encoder.waterAdditionPermission(isAllowed));
        }
    }

    /**
     * Inform the communicator the current state of the connection of the Wirma.
     * This is important because the communicator will continue to send logs and events to listeners if you continue
     * to send bytes to through the method <code>void received(byte[] bytes)</code>. resulting in corrupted logs and
     * events...
     * Note that by default, this parameters is set to false.
     *
     * @param connected true if the terminal is isConnected to the Wirma, otherwise false.
     */
    public void setConnected(final boolean connected) {
        loggerListener.log("BLUETOOTH: connection state: " + (connected ? "CONNECTED" : "NOT CONNECTED"));
        if (!connected) {
            cancelTimer();
        } else {
            if (!isSync && (state == State.WAITING_FOR_DELIVERY_NOTE || state == State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION)) {
                startTimer();
            }
        }
        this.isConnected = connected;
    }

    /** Returns the current connection state of the communicator */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * This method is the entry point of the communicator. You should pass every bytes received from the Wirma in
     * order to decode messages.
     * Note that you can pass a buffer that contains only a part of a message, as long as each bytes is conform to the
     * protocol, the communicator will keep them until to have a valid message.
     * If a buffer is corrupted for whatever reason, the communicator will consume each bytes until
     * a message conform to the protocol is decoded.
     *
     * @param bytes Bytes received from the Wirma
     */
    public void received(byte[] bytes) {
        try {
            decoder.decode(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //
    // Private stuff
    //

    // TODO: Should be private
    public void setState(State state) {
        loggerListener.log("STATE: state changed: " + state.toString());
        switch (state) {
            case WAITING_FOR_DELIVERY_NOTE:
                isSync = false;
                if (isConnected) {
                    startTimer();
                }
                break;
            case WAITING_FOR_DELIVERY_NOTE_ACCEPTATION:
                if (isSync) {
                    cancelTimer();
                }
                break;
            case DELIVERY_IN_PROGRESS:
                cancelTimer();
                if (isConnected) {
                    bytesListener.send(encoder.fake());
                    bytesListener.send(encoder.beginningOfDelivery());
                }
                break;
        }
        this.state = state;
    }

    public State currentState() {
        return state;
    }

    private void startTimer() {
        cancelTimer();

        loggerListener.log("INTERNAL: start timer");
        final TimerTask task = new TimerTask() {
            @Override
            public void run () {
                if (isConnected) {
                    bytesListener.send(encoder.fake());
                    bytesListener.send(encoder.endOfDelivery());
                }
            }
        };
        timer = new Timer();
        timer.schedule(task, 0L, RESET_STATE_IN_MILLIS);
    }

    private void cancelTimer() {
        if (timer != null) {
            loggerListener.log("INTERNAL: cancel timer");
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Message decoded
     */
    private final MessageReceivedListener messageReceivedListener = new MessageReceivedListener() {
        @Override
        public void slumpUpdated(int slump) {
            if (state == State.DELIVERY_IN_PROGRESS) {
                loggerListener.log("RECEIVED: slump updated: " + slump + " mm");
                if (isConnected) {
                    communicatorListener.slumpUpdated(slump);
                }
            } else {
                loggerListener.log("RECEIVED (IGNORED): slump updated");
            }
        }

        @Override
        public void mixingModeActivated() {
            loggerListener.log("RECEIVED: mixing mode activated");
            if (isConnected) {
                communicatorListener.mixingModeActivated();
            }
        }

        @Override
        public void unloadingModeActivated() {
            loggerListener.log("RECEIVED: unloadingModeActivated");
            if (isConnected) {
                communicatorListener.unloadingModeActivated();
            }
        }

        @Override
        public void waterAdded(int volume, WaterAdditionMode additionMode) {
            if (state == State.DELIVERY_IN_PROGRESS) {
                loggerListener.log("RECEIVED: water added: " + volume + "L, additionMode: " + additionMode.toString());
                if (isConnected) {
                    communicatorListener.waterAdded(volume, additionMode);
                }
            } else {
                loggerListener.log("RECEIVED (IGNORED): water added");
            }
        }

        @Override
        public void waterAdditionRequest(int volume) {
            if (state == State.DELIVERY_IN_PROGRESS) {
                loggerListener.log("RECEIVED: water addition request: " + volume + " L");
                if (isConnected) {
                    communicatorListener.waterAdditionRequest(volume);
                }
            } else {
                loggerListener.log("RECEIVED (IGNORED):: water addition request");
            }
        }

        @Override
        public void waterAdditionBegan() {
            loggerListener.log("RECEIVED: water addition began");
            if (isConnected) {
                communicatorListener.waterAdditionBegan();
            }
        }

        @Override
        public void waterAdditionEnd() {
            loggerListener.log("RECEIVED: water addition end");
            if (isConnected) {
                communicatorListener.waterAdditionEnd();
            }
        }

        @Override
        public void alarmWaterAdditionBlocked() {
            loggerListener.log("RECEIVED: ALARM: water addition blocked");
            if (isConnected) {
                communicatorListener.alarmWaterAdditionBlocked();
            }
        }

        @Override
        public void truckParametersRequest() {
            loggerListener.log("RECEIVED: truck parameters request");
            if (truckParameters != null) {
                if (isConnected) {
                    bytesListener.send(encoder.fake());
                    bytesListener.send(encoder.truckParameters(truckParameters));
                }
            } else {
                loggerListener.log("    WARNING: truck parameters was not set");
            }
        }

        @Override
        public void truckParametersReceived() {
            loggerListener.log("RECEIVED: truck parameters received");
        }

        @Override
        public void deliveryParametersRequest() {
            cancelTimer();
            if (state == State.WAITING_FOR_DELIVERY_NOTE || state == State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION) {
                isSync = true;
            }
            if (state == State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION || state == State.DELIVERY_IN_PROGRESS) {
                loggerListener.log("RECEIVED: delivery parameters request");
                if (deliveryParameters != null) {
                    if (isConnected) {
                        bytesListener.send(encoder.fake());
                        bytesListener.send(encoder.deliveryParameters(deliveryParameters));
                    }
                } else {
                    loggerListener.log("    WARNING: delivery parameters was not set");
                }
            } else {
                loggerListener.log("RECEIVED (IGNORED): delivery parameters request");
            }
        }

        @Override
        public void deliveryParametersReceived() {
            loggerListener.log("RECEIVED: delivery parameters received");
        }

        @Override
        public void deliveryValidationRequest() {
            loggerListener.log("RECEIVED: delivery validation request");
            if (state == State.DELIVERY_IN_PROGRESS && isConnected) {
                bytesListener.send(encoder.fake());
                bytesListener.send(encoder.beginningOfDelivery());
            }
        }

        @Override
        public void deliveryValidationReceived() {
            loggerListener.log("RECEIVED: delivery validation received");
        }

        @Override
        public void stateChanged(int step, int subStep) {
            loggerListener.log("RECEIVED: state changed: step=" + step + ", subStep=" + subStep);
            if (isConnected) {
                communicatorListener.stateChanged(step, subStep);
            }
        }

        @Override
        public void traceDebug(String trace) {
            loggerListener.log("RECEIVED: trace debug: " + trace);
        }

        @Override
        public void rawData(int inPressure, int outPressure, int interval, boolean buttonHold) {
            loggerListener.log("RECEIVED: raw data (inPressure: " + inPressure + ", outPressure:" + outPressure + ", interval: " + interval + ", buttonHold: " + (buttonHold ? "YES" : "NO") + ")");
        }

        @Override
        public void derivedData(RotationDirection rotationDirection, boolean slumpFrameStable, int currentFrameSize, int expectedFrameSize) {
            loggerListener.log("RECEIVED: derived data (rotationDirection: " + rotationDirection.toString() + ", stable: " + (slumpFrameStable ? "YES" : "NO") + ", currentFrameSize: " + currentFrameSize + ", expectedFrameSize: " + expectedFrameSize + ")");
        }

        @Override
        public void internData(boolean inSensorConnected, boolean outSensorConnected, boolean speedTooLow, boolean speedTooHigh, boolean commandEP1Activated, boolean commandVA1Activated) {
            loggerListener.log("RECEIVED: intern data (inSensorConnected: " + (inSensorConnected ? "YES" : "NO") + ", outSensorConnected: " + (outSensorConnected ? "YES" : "NO") + ", speedTooLow: " + (speedTooLow ? "YES" : "NO") + ", speedTooHigh: " + (speedTooHigh ? "YES" : "NO") + ", commandEP1Activated: " + (commandEP1Activated ? "YES" : "NO") + ", commandVA1Activated: " + (commandVA1Activated ? "YES" : "NO") + ")");
        }

        @Override
        public void calibrationData(float inPressure, float outPressure, float rotationSpeed) {
            if (state == State.DELIVERY_IN_PROGRESS) {
                loggerListener.log("RECEIVED: calibration data (inPressure: " + inPressure + ", outPressure:" + outPressure + ", " +
                        "rotationSpeed: " + rotationSpeed + " tr/min)");
                if (isConnected) {
                    communicatorListener.calibrationData(inPressure, outPressure, rotationSpeed);
                }
            } else {
                loggerListener.log("RECEIVED (IGNORED): calibration data");
            }
        }

        @Override
        public void alarmWaterMax() {
            loggerListener.log("RECEIVED: ALARM: water max");
            if (isConnected) {
                communicatorListener.alarmWaterMax();
            }
        }

        @Override
        public void alarmFlowageError() {
            loggerListener.log("RECEIVED: ALARM: flowage error");
            if (isConnected) {
                communicatorListener.alarmFlowageError();
            }
        }

        @Override
        public void alarmCountingError() {
            loggerListener.log("RECEIVED: ALARM: counting error");
            if (isConnected) {
                communicatorListener.alarmCountingError();
            }
        }

        @Override
        public void inputSensorConnectionChanged(boolean connected) {
            loggerListener.log("RECEIVED: input sensor connection changed: " + (connected ? "IS CONNECTED" : "NOT CONNECTED"));
            if (isConnected) {
                communicatorListener.inputSensorConnectionChanged(connected);
            }
        }

        @Override
        public void outputSensorConnectionChanged(boolean connected) {
            loggerListener.log("RECEIVED: output sensor connection changed: " + (connected ? "IS CONNECTED" : "NOT CONNECTED"));
            if (isConnected) {
                communicatorListener.outputSensorConnectionChanged(connected);
            }
        }

        @Override
        public void speedSensorHasExceedMinThreshold(boolean thresholdExceed) {
            loggerListener.log("RECEIVED: speed sensor has exceed min threshold: " + (thresholdExceed ? "YES" : "NO"));
            if (isConnected) {
                communicatorListener.speedSensorHasExceedMinThreshold(thresholdExceed);
            }
        }

        @Override
        public void speedSensorHasExceedMaxThreshold(boolean thresholdExceed) {
            loggerListener.log("RECEIVED: speed sensor has exceed max threshold: " + (thresholdExceed ? "YES" : "NO"));
            if (isConnected) {
                communicatorListener.speedSensorHasExceedMaxThreshold(thresholdExceed);
            }
        }
    };

    /**
     * Message encoded
     */
    private final MessageSentListener messageSentListener = new MessageSentListener() {
        @Override
        public void targetSlump(int value, byte[] bytes) {
            loggerListener.log("SENT: target slump: " + value + " mm\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void maximumWater(int value, byte[] bytes) {
            loggerListener.log("SENT: maximum water: " + value + " L\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void waterAdditionPermission(boolean isAllowed, byte[] bytes) {
            loggerListener.log("SENT: water addition allowed: " + (isAllowed ? "YES" : "NO") + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void changeExternalDisplayState(boolean isActivated, byte[] bytes) {
            loggerListener.log("SENT: change external display state: " + (isActivated ? "ACTIVATED" : "NOT ACTIVATED") + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void endOfDelivery(byte[] bytes) {
            loggerListener.log("SENT: end of delivery\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void beginningOfDelivery(byte[] bytes) {
            loggerListener.log("SENT: beginning of delivery\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void loadVolume(double value, byte[] bytes) {
            loggerListener.log("SENT: load volume: " + value + " m3" + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void parameterT1(double value, byte[] bytes) {
            loggerListener.log("SENT: parameter T1: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void parameterA11(double value, byte[] bytes) {
            loggerListener.log("SENT: parameter A11: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void parameterA12(double value, byte[] bytes) {
            loggerListener.log("SENT: parameter A12: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void parameterA13(double value, byte[] bytes) {
            loggerListener.log("SENT: parameter A13: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void magnetQuantity(int value, byte[] bytes) {
            loggerListener.log("SENT: magnet quantity: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void timePump(int value, byte[] bytes) {
            loggerListener.log("SENT: time pump: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void timeDelayDriver(int value, byte[] bytes) {
            loggerListener.log("SENT: time delay driver: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void pulseNumber(int value, byte[] bytes) {
            loggerListener.log("SENT: pulse number: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void flowmeterFrequency(int value, byte[] bytes) {
            loggerListener.log("SENT: flowmeter frequency: " + value + "Hz\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void commandPumpMode(TruckParameters.CommandPumpMode commandPumpMode, byte[] bytes) {
            loggerListener.log("SENT: command pump mode: " + commandPumpMode.toString() + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void calibrationInputSensorA(double value, byte[] bytes) {
            loggerListener.log("SENT: calibration input sensor A: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void calibrationOutputSensorA(double value, byte[] bytes) {
            loggerListener.log("SENT: calibration output sensor A: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void calibrationInputSensorB(double value, byte[] bytes) {
            loggerListener.log("SENT: calibration input  sensor B: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void calibrationOutputSensorB(double value, byte[] bytes) {
            loggerListener.log("SENT: calibration output sensor B: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void openingTimeEV1(int value, byte[] bytes) {
            loggerListener.log("SENT: opening time EV1: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void openingTimeVA1(int value, byte[] bytes) {
            loggerListener.log("SENT: opening time VA1: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void countingTolerance(int value, byte[] bytes) {
            loggerListener.log("SENT: counting tolerance: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void waitingDurationAfterWaterAddition(int value, byte[] bytes) {
            loggerListener.log("SENT: waiting duration after water addition: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void maxDelayBeforeFlowage(int value, byte[] bytes) {
            loggerListener.log("SENT: max delay before flowage: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void maxFlowageError(int value, byte[] bytes) {
            loggerListener.log("SENT: max flowage error: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void maxCountingError(int value, byte[] bytes) {
            loggerListener.log("SENT: max couting error: " + value + "\n    " + Convert.bytesToHex(bytes));
        }

        @Override
        public void fake(byte[] bytes) {
            loggerListener.log("SENT: trame bidon\n    " + Convert.bytesToHex(bytes));
        }
    };

    /**
     * Progression of the Decoder
     */
    private final ProgressListener progressListener= new ProgressListener() {
        @Override
        public void timeout() {
            loggerListener.log("PROCESS: previous state has expired -> reset to HEADER");
        }

        @Override
        public void willDecode(byte[] buff) {
            loggerListener.log("PROCESS: will decode:\n    " + Convert.bytesToHex(buff));
        }

        @Override
        public void willProcessByte(ProgressState state, byte b) {
//            loggerListener.log("PROCESS: current state: " + state.toString() + ", will process byte: " + Convert.byteToHex(b));
        }

        @Override
        public void parsingFailed(ParsingError errorType, byte b) {
            loggerListener.log("PROCESS: " + errorType.toString() + " for byte: " + Convert.byteToHex(b));
        }
    };
}
