package com.lafarge.truckmix.common.models;

import com.lafarge.truckmix.common.enums.CommandPumpMode;

import java.io.Serializable;

/**
 * This class contains all information the calculator need for calibration.
 */
public class TruckParameters implements Serializable {

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
        return "TruckParameters{"
                + "T1=" + T1
                + ",A11=" + A11
                + ",A12=" + A12
                + ",A13=" + A13
                + ",magnetQuantity=" + magnetQuantity
                + ",timePump=" + timePump
                + ",timeDelayDriver=" + timeDelayDriver
                + ",pulseNumber=" + pulseNumber
                + ",flowmeterFrequency=" + flowmeterFrequency
                + ",commandPumpMode=" + commandPumpMode
                + ",calibrationInputSensorA=" + calibrationInputSensorA
                + ",calibrationInputSensorB=" + calibrationInputSensorB
                + ",calibrationOutputSensorA=" + calibrationOutputSensorA
                + ",calibrationOutputSensorB=" + calibrationOutputSensorB
                + ",openingTimeEV1=" + openingTimeEV1
                + ",openingTimeVA1=" + openingTimeVA1
                + ",toleranceCounting=" + toleranceCounting
                + ",waitingDurationAfterWaterAddition=" + waitingDurationAfterWaterAddition
                + ",maxDelayBeforeFlowage=" + maxDelayBeforeFlowage
                + ",maxFlowageError=" + maxFlowageError
                + ",maxCountingError=" + maxCountingError
                + '}';
    }

    public static class Builder {
        private double T1;
        private double A11;
        private double A12;
        private double A13;
        private int magnetQuantity;
        private int timePump;
        private int timeDelayDriver;
        private int pulseNumber;
        private int flowmeterFrequency;
        private CommandPumpMode commandPumpMode;
        private double calibrationInputSensorA;
        private double calibrationInputSensorB;
        private double calibrationOutputSensorA;
        private double calibrationOutputSensorB;
        private int openingTimeEV1;
        private int openingTimeVA1;
        private int toleranceCounting;
        private int waitingDurationAfterWaterAddition;
        private int maxDelayBeforeFlowage;
        private int maxFlowageError;
        private int maxCountingError;

        public Builder setT1(double T1) {
            this.T1 = T1;
            return this;
        }

        public Builder setA11(double A11) {
            this.A11 = A11;
            return this;
        }

        public Builder setA12(double A12) {
            this.A12 = A12;
            return this;
        }

        public Builder setA13(double A13) {
            this.A13 = A13;
            return this;
        }

        public Builder setMagnetQuantity(int magnetQuantity) {
            this.magnetQuantity = magnetQuantity;
            return this;
        }

        public Builder setTimePump(int timePump) {
            this.timePump = timePump;
            return this;
        }

        public Builder setTimeDelayDriver(int timeDelayDriver) {
            this.timeDelayDriver = timeDelayDriver;
            return this;
        }

        public Builder setPulseNumber(int pulseNumber) {
            this.pulseNumber = pulseNumber;
            return this;
        }

        public Builder setFlowmeterFrequency(int flowmeterFrequency) {
            this.flowmeterFrequency = flowmeterFrequency;
            return this;
        }

        public Builder setCommandPumpMode(CommandPumpMode commandPumpMode) {
            this.commandPumpMode = commandPumpMode;
            return this;
        }

        public Builder setCalibrationInputSensorA(double calibrationInputSensorA) {
            this.calibrationInputSensorA = calibrationInputSensorA;
            return this;
        }

        public Builder setCalibrationInputSensorB(double calibrationInputSensorB) {
            this.calibrationInputSensorB = calibrationInputSensorB;
            return this;
        }

        public Builder setCalibrationOutputSensorA(double calibrationOutputSensorA) {
            this.calibrationOutputSensorA = calibrationOutputSensorA;
            return this;
        }

        public Builder setCalibrationOutputSensorB(double calibrationOutputSensorB) {
            this.calibrationOutputSensorB = calibrationOutputSensorB;
            return this;
        }

        public Builder setOpeningTimeEV1(int openingTimeEV1) {
            this.openingTimeEV1 = openingTimeEV1;
            return this;
        }

        public Builder setOpeningTimeVA1(int openingTimeVA1) {
            this.openingTimeVA1 = openingTimeVA1;
            return this;
        }

        public Builder setToleranceCounting(int toleranceCounting) {
            this.toleranceCounting = toleranceCounting;
            return this;
        }

        public Builder setWaitingDurationAfterWaterAddition(int waitingDurationAfterWaterAddition) {
            this.waitingDurationAfterWaterAddition = waitingDurationAfterWaterAddition;
            return this;
        }

        public Builder setMaxDelayBeforeFlowage(int maxDelayBeforeFlowage) {
            this.maxDelayBeforeFlowage = maxDelayBeforeFlowage;
            return this;
        }

        public Builder setMaxFlowageError(int maxFlowageError) {
            this.maxFlowageError = maxFlowageError;
            return this;
        }

        public Builder setMaxCountingError(int maxCountingError) {
            this.maxCountingError = maxCountingError;
            return this;
        }

        public TruckParameters build() {
            return new TruckParameters(T1, A11, A12, A13, magnetQuantity, timePump, timeDelayDriver, pulseNumber, flowmeterFrequency, commandPumpMode, calibrationInputSensorA, calibrationInputSensorB, calibrationOutputSensorA, calibrationOutputSensorB, openingTimeEV1, openingTimeVA1, toleranceCounting, waitingDurationAfterWaterAddition, maxDelayBeforeFlowage, maxFlowageError, maxCountingError);
        }
    }
}
