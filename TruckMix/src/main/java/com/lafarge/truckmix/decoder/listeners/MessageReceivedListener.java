package com.lafarge.truckmix.decoder.listeners;

/**
 * This interface list all message decoded received from the calculator.
 */
public interface MessageReceivedListener {

    enum WaterAdditionMode {
        MANUAL,
        AUTO
    }

    enum RotationDirection {
        MIXING,
        UNLOADING
    }

    void slumpUpdated(int slump);
    void temperatureUpdated(float temperature);
    void mixingModeActivated();
    void unloadingModeActivated();
    void waterAdded(int volume, WaterAdditionMode additionMode);
    void waterAdditionRequest(int volume);
    void waterAdditionBegan();
    void waterAdditionEnd();
    void alarmWaterAdditionBlocked();
    void truckParametersRequest();
    void truckParametersReceived();
    void deliveryParametersRequest();
    void deliveryParametersReceived();
    void deliveryValidationRequest();
    void deliveryValidationReceived();
    void stateChanged(int step, int subStep);
    void traceDebug(String trace);
    void rawData(int inputPressure, int outputPressure, int interval, boolean buttonHold);
    void derivedData(RotationDirection rotationDirection, boolean slumpFrameStable, int currentFrameSize, int expectedFrameSize);
    void internData(boolean inSensorConnected, boolean outSensorConnected, boolean speedTooLow, boolean speedTooHigh, boolean commandEP1Activated, boolean commandVA1Activated);
    void calibrationData(float inputPressure, float outputPressure, float rotationSpeed);
    void alarmWaterMax();
    void alarmFlowageError();
    void alarmCountingError();
    void inputSensorConnectionChanged(boolean connected);
    void outputSensorConnectionChanged(boolean connected);
    void speedSensorHasExceedMinThreshold(boolean thresholdExceed);
    void speedSensorHasExceedMaxThreshold(boolean thresholdExceed);
}
