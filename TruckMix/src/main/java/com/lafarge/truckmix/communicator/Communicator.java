package com.lafarge.truckmix.communicator;

import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.events.EventFactory;
import com.lafarge.truckmix.communicator.listeners.CommunicatorBytesListener;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.EventListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;
import com.lafarge.truckmix.decoder.Decoder;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.ProgressListener;
import com.lafarge.truckmix.encoder.Encoder;
import com.lafarge.truckmix.encoder.listeners.MessageSentListener;
import com.lafarge.truckmix.utils.Convert;

import java.io.IOException;

/**
 * This class is the entry point of the whole library, it is responsible of the communication between the client
 * and the calculator. All bytes to send to the calculator will be given here (through listener) and all bytes received will be
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
     * Interval between two send of "end of delivery" in order to keep the calculator isSync with us
     */
    public static final long RESET_STATE_IN_MILLIS = 10 * 1000;

    // Encoder/Decoder
    private final Encoder encoder;
    private final Decoder decoder;

    // Listener
    private final CommunicatorBytesListener bytesListener;
    private final CommunicatorListener communicatorListener;
    private final LoggerListener loggerListener;
    private final EventListener eventListener;

    // Parameters
    private TruckParameters truckParameters;
    private DeliveryParameters deliveryParameters;

    // Options
    private boolean waterRequestAllowed;
    private boolean qualityTrackingActivated;

    // Current state
    private State state;
    private boolean isSync;
    private boolean isConnected;
    private int currentSlump;
    MessageReceivedListener.RotationDirection currentRotation;

    // Other
    private final Scheduler scheduler;

    /**
     * Constructor should be called only once per session
     *
     * @param bytesListener Consumer should implement this listener to know what bytes to send
     * @param communicatorListener Listener of functional events e.g. slump updates or sensor's state changes
     * @param loggerListener Listener of logs, consumer will have logs about what was decoded, encoded, and errors
     *                       while parsing.
     * @param eventListener Listener of events that happen while a delivery
     * @throws IllegalArgumentException If one of the parameters is null
     */
    public Communicator(CommunicatorBytesListener bytesListener, CommunicatorListener communicatorListener,
                        LoggerListener loggerListener, EventListener eventListener) {
        this(bytesListener, communicatorListener, loggerListener, eventListener, new Scheduler(RESET_STATE_IN_MILLIS));
    }

    public Communicator(CommunicatorBytesListener bytesListener, CommunicatorListener communicatorListener,
                        LoggerListener loggerListener, EventListener eventListener, Scheduler scheduler) {
        if (bytesListener == null) throw new IllegalArgumentException("bytesListener can't be null");
        if (communicatorListener == null) throw new IllegalArgumentException("communicatorListener can't be null");
        if (loggerListener == null) throw new IllegalArgumentException("loggerListener can't be null");
        if (eventListener == null) throw new IllegalArgumentException("eventListener can't be null");

        this.bytesListener = bytesListener;
        this.communicatorListener = communicatorListener;
        this.loggerListener = loggerListener;
        this.eventListener = eventListener;
        this.encoder = new Encoder(messageSentListener);
        this.decoder = new Decoder(messageReceivedListener, progressListener);
        this.state = State.WAITING_FOR_DELIVERY_NOTE;
        this.isConnected = false;
        this.scheduler = scheduler;
    }

    //
    // List of actions
    //

    /**
     * Set the truck parameters. Will be send next time the calculator request them.
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
     * Set the delivery parameters. Will be send next time the calculator request them.
     *
     * @param parameters The delivery parameters
     * @throws IllegalArgumentException If parameters is null
     */
    public void deliveryNoteReceived(DeliveryParameters parameters) {
        if (parameters == null) throw new IllegalArgumentException("DeliveryParameters can't be null");
        if (state == State.WAITING_FOR_DELIVERY_NOTE) {
            loggerListener.log("ACTION: delivery note received:\n    " + parameters.toString());
            deliveryParameters = parameters;
            setState(State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION);
        } else {
            loggerListener.log("ACTION (IGNORED): delivery note received");
        }
    }

    /**
     * Tell to the calculator that we accepted or not the delivery. Note that you should call this method only after having
     * given delivery parameters.
     *
     * @param accepted true to tell the calculator to start a delivery, otherwise no.
     */
    public void acceptDelivery(boolean accepted) {
        if (state == State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION) {
            loggerListener.log("ACTION: accept delivery: " + (accepted ? "YES" : "NO"));
            setState(accepted ? State.DELIVERY_IN_PROGRESS : State.WAITING_FOR_DELIVERY_NOTE);
            if (qualityTrackingActivated) {
                eventListener.onNewEvents(EventFactory.createStartDeliveryEvent(accepted));
                eventListener.onNewEvents(EventFactory.createBluetoothConnectionEvent(isConnected));
            }
        } else {
            loggerListener.log("ACTION (IGNORED): accept delivery: " + (accepted ? "YES" : "NO"));
        }
    }

    /**
     * Tell the calculator to end the current delivery in progress.
     */
    public void endDelivery() {
        if (state == State.DELIVERY_IN_PROGRESS) {
            loggerListener.log("ACTION: end delivery");
            setState(State.WAITING_FOR_DELIVERY_NOTE);
            if (qualityTrackingActivated) {
                eventListener.onNewEvents(EventFactory.createEndOfDeliveryEvent(currentSlump));
            }
        } else {
            loggerListener.log("ACTION (IGNORED): end delivery");
        }
    }

    /**
     * Change the external display state of the truck.
     *
     * @param isActivated true to activate it, otherwise false to deactivate it.
     */
    public void changeExternalDisplayState(boolean isActivated) {
        if (isConnected) {
            loggerListener.log("ACTION: change external display state: " + (isActivated ? "ACTIVATED" : "NOT ACTIVATED"));
            bytesListener.send(encoder.fake());
            bytesListener.send(encoder.changeExternalDisplayState(isActivated));
        } else {
            loggerListener.log("ACTION (IGNORED): change external display state: " + (isActivated ? "ACTIVATED" : "NOT ACTIVATED"));
        }
    }

    /**
     * Give calculator the permission to add water or note after. You should call this method only if you received a request
     * to add water.
     *
     * @param isAllowed
     */
    public void allowWaterAddition(boolean isAllowed) {
        // TODO: check if there was a water addition request before sending
        if (isConnected && waterRequestAllowed) {
            loggerListener.log("ACTION: allow water addition: " + (isAllowed ? "ALLOWED" : "NOT ALLOWED"));
            bytesListener.send(encoder.fake());
            bytesListener.send(encoder.waterAdditionPermission(isAllowed));
        }
    }

    /**
     * Inform the communicator the current state of the connection of the calculator.
     * This is important because the communicator will continue to send logs and events to listeners if you continue
     * to send bytes to through the method <code>void received(byte[] bytes)</code>. resulting in corrupted logs and
     * events...
     * Note that by default, this parameters is set to false.
     * By default, the Communicator is not connected.
     *
     * @param isConnected true if the terminal is connected to the calculator, otherwise false.
     */
    public void setConnected(boolean isConnected) {
        loggerListener.log("BLUETOOTH: connection state: " + (isConnected ? "CONNECTED" : "NOT CONNECTED"));
        this.isConnected = isConnected;
        if (!isConnected) {
            cancelTimer();
        } else if (!isSync && state != State.DELIVERY_IN_PROGRESS) {
            startTimer();
        }
        if (state == State.DELIVERY_IN_PROGRESS && qualityTrackingActivated) {
            eventListener.onNewEvents(EventFactory.createBluetoothConnectionEvent(isConnected));
        }
    }

    /** Returns the current connection state of the Communicator */
    public boolean isConnected() {
        return isConnected;
    }

    /** Returns the current slump sent by the calculator */
    public int currentSlump() {
        return currentSlump;
    }

    /** Return the current rotation mode sent by the calculator */
    public MessageReceivedListener.RotationDirection currentRotation() {
        return currentRotation;
    }

    /**
     * Allow water actions, useful for countries that doesn't allow water addition in the concrete.
     * By default, water request is not allowed.
     *
     * @param waterRequestAllowed true if you want to interact with the water, otherwise false.
     */
    public void setWaterRequestAllowed(boolean waterRequestAllowed) {
        loggerListener.log("OPTION: water request is " + (waterRequestAllowed ? "ALLOWED" : "NOT ALLOWED"));
        this.waterRequestAllowed = waterRequestAllowed;
    }

    /** Return the state of the water request allowance */
    public boolean isWaterRequestAllowed() {
        return waterRequestAllowed;
    }

    /**
     * Activate the quality tracking, if true, events will be send to the EventListener passed in constructor.
     * By default, quality traduction is not enabled.
     *
     * @param activated true to activate the quality tracking, otherwise false.
     */
    public void setQualityTrackingActivated(boolean activated) {
        loggerListener.log("OPTION: quality tracking is " + (waterRequestAllowed ? "ENABLED" : "DISABLED"));
        this.qualityTrackingActivated = activated;
    }

    /** Return the state of the quality tracking */
    public boolean isQualityTrackingActivated() {
        return qualityTrackingActivated;
    }

    /**
     * This method is the entry point of the communicator. You should pass every bytes received from the calculator in
     * order to decode messages.
     * Note that you can pass a buffer that contains only a part of a message, as long as each bytes is conform to the
     * protocol, the communicator will keep them until to have a valid message.
     * If a buffer is corrupted for whatever reason, the communicator will consume each bytes until
     * a message conform to the protocol is decoded.
     *
     * @param bytes Bytes received from the calculator
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

    private void setState(State state) {
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
        loggerListener.log("INTERNAL: start timer");
        this.scheduler.start(new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    bytesListener.send(encoder.fake());
                    bytesListener.send(encoder.endOfDelivery());
                }
            }
        });
    }

    private void cancelTimer() {
        loggerListener.log("INTERNAL: cancel timer");
        this.scheduler.reset();
    }

    /**
     * Message decoded
     */
    private final MessageReceivedListener messageReceivedListener = new MessageReceivedListener() {
        @Override
        public void slumpUpdated(int slump) {
            if (isConnected) {
                if (state == State.DELIVERY_IN_PROGRESS) {
                    loggerListener.log("RECEIVED: slump updated: " + slump + " mm");
                    currentSlump = slump;
                    communicatorListener.slumpUpdated(slump);
                    if (qualityTrackingActivated) {
                        eventListener.onNewEvents(EventFactory.createNewSlumpEvent(slump));
                    }
                } else {
                    loggerListener.log("RECEIVED (IGNORED): slump updated");
                }
            }
        }

        @Override
        public void mixingModeActivated() {
            if (isConnected) {
                loggerListener.log("RECEIVED: mixing mode activated");
                communicatorListener.mixingModeActivated();
                if (currentRotation != RotationDirection.MIXING) {
                    currentRotation = RotationDirection.MIXING;
                    if (state == State.DELIVERY_IN_PROGRESS && qualityTrackingActivated) {
                        eventListener.onNewEvents(EventFactory.createMixerTransitionEvent(RotationDirection.MIXING));
                    }
                }
            }
        }

        @Override
        public void unloadingModeActivated() {
            if (isConnected) {
                loggerListener.log("RECEIVED: unloadingModeActivated");
                communicatorListener.unloadingModeActivated();
                if (currentRotation != RotationDirection.UNLOADING) {
                    currentRotation = RotationDirection.UNLOADING;
                    if (state == State.DELIVERY_IN_PROGRESS && qualityTrackingActivated) {
                        eventListener.onNewEvents(EventFactory.createMixerTransitionEvent(RotationDirection.UNLOADING));
                    }
                }
            }
        }

        @Override
        public void waterAdded(int volume, WaterAdditionMode additionMode) {
            if (isConnected && waterRequestAllowed) {
                if (state == State.DELIVERY_IN_PROGRESS && waterRequestAllowed) {
                    loggerListener.log("RECEIVED: water added: " + volume + "L, additionMode: " + additionMode.toString());
                    communicatorListener.waterAdded(volume, additionMode);
                } else {
                    loggerListener.log("RECEIVED (IGNORED): water added");
                }
            }
        }

        @Override
        public void waterAdditionRequest(int volume) {
            if (isConnected && waterRequestAllowed) {
                if (state == State.DELIVERY_IN_PROGRESS) {
                    loggerListener.log("RECEIVED: water addition request: " + volume + " L");
                    communicatorListener.waterAdditionRequest(volume);
                } else {
                    loggerListener.log("RECEIVED (IGNORED): water addition request");
                }
            }
        }

        @Override
        public void waterAdditionBegan() {
            if (isConnected && waterRequestAllowed) {
                loggerListener.log("RECEIVED: water addition began");
                communicatorListener.waterAdditionBegan();
            }
        }

        @Override
        public void waterAdditionEnd() {
            if (isConnected && waterRequestAllowed) {
                loggerListener.log("RECEIVED: water addition end");
                communicatorListener.waterAdditionEnd();
            }
        }

        @Override
        public void alarmWaterAdditionBlocked() {
            if (isConnected && waterRequestAllowed) {
                loggerListener.log("RECEIVED: ALARM: water addition blocked");
                communicatorListener.alarmWaterAdditionBlocked();
            }
        }

        @Override
        public void truckParametersRequest() {
            if (isConnected) {
                loggerListener.log("RECEIVED: truck parameters request");
                if (truckParameters != null) {
                    bytesListener.send(encoder.fake());
                    bytesListener.send(encoder.truckParameters(truckParameters));
                } else {
                    loggerListener.log("    WARNING: truck parameters was not set");
                }
            }
        }

        @Override
        public void truckParametersReceived() {
            if (isConnected) {
                loggerListener.log("RECEIVED: truck parameters received");
            }
        }

        @Override
        public void deliveryParametersRequest() {
            if (isConnected) {
                cancelTimer();
                if (state != State.DELIVERY_IN_PROGRESS) {
                    isSync = true;
                }
                if (state != State.WAITING_FOR_DELIVERY_NOTE) {
                    loggerListener.log("RECEIVED: delivery parameters request");
                    if (deliveryParameters != null) {
                        bytesListener.send(encoder.fake());
                        bytesListener.send(encoder.deliveryParameters(deliveryParameters));
                    } else {
                        loggerListener.log("    WARNING: delivery parameters was not set");
                    }
                } else {
                    loggerListener.log("RECEIVED (IGNORED): delivery parameters request");
                }
            }
        }

        @Override
        public void deliveryParametersReceived() {
            if (isConnected) {
                loggerListener.log("RECEIVED: delivery parameters received");
            }
        }

        @Override
        public void deliveryValidationRequest() {
            if (isConnected) {
                if (state == State.DELIVERY_IN_PROGRESS) {
                    loggerListener.log("RECEIVED: delivery validation request");
                    bytesListener.send(encoder.fake());
                    bytesListener.send(encoder.beginningOfDelivery());
                } else {
                    loggerListener.log("RECEIVED (IGNORED): delivery validation request");
                }
            }
        }

        @Override
        public void deliveryValidationReceived() {
            if (isConnected) {
                loggerListener.log("RECEIVED: delivery validation received");
            }
        }

        @Override
        public void stateChanged(int step, int subStep) {
            if (isConnected) {
                loggerListener.log("RECEIVED: state changed: step=" + step + ", subStep=" + subStep);
                communicatorListener.stateChanged(step, subStep);
            }
        }

        @Override
        public void traceDebug(String trace) {
            if (isConnected) {
                loggerListener.log("RECEIVED: trace debug: " + trace);
            }
        }

        @Override
        public void rawData(int inPressure, int outPressure, int interval, boolean buttonHold) {
            if (isConnected) {
                loggerListener.log("RECEIVED: raw data (inPressure: " + inPressure + ", outPressure:" + outPressure + ", interval: " + interval + ", buttonHold: " + (buttonHold ? "YES" : "NO") + ")");
            }
        }

        @Override
        public void derivedData(RotationDirection rotationDirection, boolean slumpFrameStable, int currentFrameSize, int expectedFrameSize) {
            if (isConnected) {
                loggerListener.log("RECEIVED: derived data (rotationDirection: " + rotationDirection.toString() + ", stable: " + (slumpFrameStable ? "YES" : "NO") + ", currentFrameSize: " + currentFrameSize + ", expectedFrameSize: " + expectedFrameSize + ")");
            }
        }

        @Override
        public void internData(boolean inSensorConnected, boolean outSensorConnected, boolean speedTooLow, boolean speedTooHigh, boolean commandEP1Activated, boolean commandVA1Activated) {
            if (isConnected) {
                loggerListener.log("RECEIVED: intern data (inSensorConnected: " + (inSensorConnected ? "YES" : "NO") + ", outSensorConnected: " + (outSensorConnected ? "YES" : "NO") + ", speedTooLow: " + (speedTooLow ? "YES" : "NO") + ", speedTooHigh: " + (speedTooHigh ? "YES" : "NO") + ", commandEP1Activated: " + (commandEP1Activated ? "YES" : "NO") + ", commandVA1Activated: " + (commandVA1Activated ? "YES" : "NO") + ")");
            }
        }

        @Override
        public void calibrationData(float inPressure, float outPressure, float rotationSpeed) {
            if (isConnected) {
                if (state == State.DELIVERY_IN_PROGRESS) {
                    loggerListener.log("RECEIVED: calibration data (inPressure: " + inPressure + ", outPressure:" + outPressure + ", " +
                            "rotationSpeed: " + rotationSpeed + " tr/min)");
                    communicatorListener.calibrationData(inPressure, outPressure, rotationSpeed);
                    if (qualityTrackingActivated) {
                        eventListener.onNewEvents(EventFactory.createRotationSpeedEvent(rotationSpeed));
                        eventListener.onNewEvents(EventFactory.createInputPressureEvent(inPressure));
                        eventListener.onNewEvents(EventFactory.createOutputPressureEvent(outPressure));
                    }
                } else {
                    loggerListener.log("RECEIVED (IGNORED): calibration data");
                }
            }
        }

        @Override
        public void alarmWaterMax() {
            if (isConnected && waterRequestAllowed) {
                loggerListener.log("RECEIVED: ALARM: water max");
                communicatorListener.alarmWaterMax();
            }
        }

        @Override
        public void alarmFlowageError() {
            if (isConnected && waterRequestAllowed) {
                loggerListener.log("RECEIVED: ALARM: flowage error");
                communicatorListener.alarmFlowageError();
            }
        }

        @Override
        public void alarmCountingError() {
            if (isConnected && waterRequestAllowed) {
                loggerListener.log("RECEIVED: ALARM: counting error");
                communicatorListener.alarmCountingError();
            }
        }

        @Override
        public void inputSensorConnectionChanged(boolean connected) {
            if (isConnected) {
                loggerListener.log("RECEIVED: input sensor connection changed: " + (connected ? "IS CONNECTED" : "NOT CONNECTED"));
                communicatorListener.inputSensorConnectionChanged(connected);
            }
        }

        @Override
        public void outputSensorConnectionChanged(boolean connected) {
            if (isConnected) {
                loggerListener.log("RECEIVED: output sensor connection changed: " + (connected ? "IS CONNECTED" : "NOT CONNECTED"));
                communicatorListener.outputSensorConnectionChanged(connected);
            }
        }

        @Override
        public void speedSensorHasExceedMinThreshold(boolean thresholdExceed) {
            if (isConnected) {
                loggerListener.log("RECEIVED: speed sensor has exceed min threshold: " + (thresholdExceed ? "YES" : "NO"));
                communicatorListener.speedSensorHasExceedMinThreshold(thresholdExceed);
                if (state == State.DELIVERY_IN_PROGRESS && qualityTrackingActivated) {
                    eventListener.onNewEvents(EventFactory.createRotationSpeedLimitMinEvent(thresholdExceed));
                }
            }
        }

        @Override
        public void speedSensorHasExceedMaxThreshold(boolean thresholdExceed) {
            if (isConnected) {
                loggerListener.log("RECEIVED: speed sensor has exceed max threshold: " + (thresholdExceed ? "YES" : "NO"));
                communicatorListener.speedSensorHasExceedMaxThreshold(thresholdExceed);
                if (state == State.DELIVERY_IN_PROGRESS && qualityTrackingActivated) {
                    eventListener.onNewEvents(EventFactory.createRotationSpeedLimitMaxEvent(thresholdExceed));
                }
            }
        }
    };

    /**
     * Message encoded
     */
    private final MessageSentListener messageSentListener = new MessageSentListener() {
        @Override
        public void targetSlump(int value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: target slump: " + value + " mm\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void maximumWater(int value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: maximum water: " + value + " L\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void waterAdditionPermission(boolean isAllowed, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: water addition allowed: " + (isAllowed ? "YES" : "NO") + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void changeExternalDisplayState(boolean isActivated, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: change external display state: " + (isActivated ? "ACTIVATED" : "NOT ACTIVATED") + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void endOfDelivery(byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: end of delivery\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void beginningOfDelivery(byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: beginning of delivery\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void loadVolume(double value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: load volume: " + value + " m3" + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void parameterT1(double value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: parameter T1: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void parameterA11(double value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: parameter A11: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void parameterA12(double value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: parameter A12: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void parameterA13(double value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: parameter A13: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void magnetQuantity(int value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: magnet quantity: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void timePump(int value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: time pump: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void timeDelayDriver(int value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: time delay driver: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void pulseNumber(int value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: pulse number: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void flowmeterFrequency(int value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: flowmeter frequency: " + value + "Hz\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void commandPumpMode(TruckParameters.CommandPumpMode commandPumpMode, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: command pump mode: " + commandPumpMode.toString() + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void calibrationInputSensorA(double value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: calibration input sensor A: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void calibrationOutputSensorA(double value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: calibration output sensor A: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void calibrationInputSensorB(double value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: calibration input  sensor B: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void calibrationOutputSensorB(double value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: calibration output sensor B: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void openingTimeEV1(int value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: opening time EV1: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void openingTimeVA1(int value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: opening time VA1: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void countingTolerance(int value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: counting tolerance: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void waitingDurationAfterWaterAddition(int value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: waiting duration after water addition: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void maxDelayBeforeFlowage(int value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: max delay before flowage: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void maxFlowageError(int value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: max flowage error: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void maxCountingError(int value, byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: max couting error: " + value + "\n    " + Convert.bytesToHex(bytes));
            }
        }

        @Override
        public void fake(byte[] bytes) {
            if (isConnected) {
                loggerListener.log("SENT: trame bidon\n    " + Convert.bytesToHex(bytes));
            }
        }
    };

    /**
     * Progression of the Decoder
     */
    private final ProgressListener progressListener= new ProgressListener() {
        @Override
        public void timeout() {
            if (isConnected) {
                loggerListener.log("PROCESS: previous state has expired -> reset to HEADER");
            }
        }

        @Override
        public void willDecode(byte[] buff) {
            if (isConnected) {
                loggerListener.log("PROCESS: will decode:\n    " + Convert.bytesToHex(buff));
            }
        }

        @Override
        public void willProcessByte(ProgressState state, byte b) {}

        @Override
        public void parsingFailed(ParsingError errorType, byte b) {
            if (isConnected) {
                loggerListener.log("PROCESS: " + errorType.toString() + " for byte: " + Convert.byteToHex(b));
            }
        }
    };
}
