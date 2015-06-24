package com.lafarge.truckmix.common.models;

/**
 * This class contains all information the calculator need for calibration.
 */
public class TruckParameters {

    /** Represent the command pump mode */
    public enum CommandPumpMode {
        AUTO,
        SEMI_AUTO
    }

    // Member fields
    public final double T1;
    public final double A11;
    public final double A12;
    public final double A13;
    public final int magnetQuantity;
    public final int timePump;
    public final int timeDelayDriver;
    public final int pulseNumber;
    public final int flowmeterFrequency;
    public final CommandPumpMode commandPumpMode;
    public final double calibrationInputSensorA;
    public final double calibrationInputSensorB;
    public final double calibrationOutputSensorA;
    public final double calibrationOutputSensorB;
    public final int openingTimeEV1;
    public final int openingTimeVA1;
    public final int toleranceCounting;
    public final int waitingDurationAfterWaterAddition;
    public final int maxDelayBeforeFlowage;
    public final int maxFlowageError;
    public final int maxCountingError;

    /**
     * Constructor
     */
    public TruckParameters(double T1, double A11, double A12, double A13, int magnetQuantity, int timePump, int timeDelayDriver, int pulseNumber, int flowmeterFrequency, CommandPumpMode commandPumpMode, double calibrationInputSensorA, double calibrationInputSensorB, double calibrationOutputSensorA, double calibrationOutputSensorB, int openingTimeEV1, int openingTimeVA1, int toleranceCounting, int waitingDurationAfterWaterAddition, int maxDelayBeforeFlowage, int maxFlowageError, int maxCountingError) {
        this.T1 = T1;
        this.A11 = A11;
        this.A12 = A12;
        this.A13 = A13;
        this.magnetQuantity = magnetQuantity;
        this.timePump = timePump;
        this.timeDelayDriver = timeDelayDriver;
        this.pulseNumber = pulseNumber;
        this.flowmeterFrequency = flowmeterFrequency;
        this.commandPumpMode = commandPumpMode;
        this.calibrationInputSensorA = calibrationInputSensorA;
        this.calibrationInputSensorB = calibrationInputSensorB;
        this.calibrationOutputSensorA = calibrationOutputSensorA;
        this.calibrationOutputSensorB = calibrationOutputSensorB;
        this.openingTimeEV1 = openingTimeEV1;
        this.openingTimeVA1 = openingTimeVA1;
        this.toleranceCounting = toleranceCounting;
        this.waitingDurationAfterWaterAddition = waitingDurationAfterWaterAddition;
        this.maxDelayBeforeFlowage = maxDelayBeforeFlowage;
        this.maxFlowageError = maxFlowageError;
        this.maxCountingError = maxCountingError;
    }

    @Override
    public String toString() {
        return "TruckParameters{" +
                "T1=" + T1 +
                ", A11=" + A11 +
                ", A12=" + A12 +
                ", A13=" + A13 +
                ", magnetQuantity=" + magnetQuantity +
                ", timePump=" + timePump +
                ", timeDelayDriver=" + timeDelayDriver +
                ", pulseNumber=" + pulseNumber +
                ", flowmeterFrequency=" + flowmeterFrequency +
                ", commandPumpMode=" + commandPumpMode +
                ", calibrationInputSensorA=" + calibrationInputSensorA +
                ", calibrationInputSensorB=" + calibrationInputSensorB +
                ", calibrationOutputSensorA=" + calibrationOutputSensorA +
                ", calibrationOutputSensorB=" + calibrationOutputSensorB +
                ", openingTimeEV1=" + openingTimeEV1 +
                ", openingTimeVA1=" + openingTimeVA1 +
                ", toleranceCounting=" + toleranceCounting +
                ", waitingDurationAfterWaterAddition=" + waitingDurationAfterWaterAddition +
                ", maxDelayBeforeFlowage=" + maxDelayBeforeFlowage +
                ", maxFlowageError=" + maxFlowageError +
                ", maxCountingError=" + maxCountingError +
                '}';
    }
}
