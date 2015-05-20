package com.lafarge.tm;

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
    void rawData(int inPressure, int outPressure, int interval, boolean buttonHold);
    void derivedData(RotationDirection rotationDirection, boolean slumpFrameStable, int currentFrameSize, int expectedFrameSize);
    void internData(boolean inSensorConnected, boolean outSensorConnected, boolean speedTooLow, boolean speedTooHigh, boolean commandEP1Activated, boolean commandVA1Activated);
    void calibrationData(float inPressure, float outPressure, float rotationSpeed);
    void alarmWaterMax();
    void alarmFlowageError();
    void alarmCountingError();
    void inputSensorConnectionChanged(boolean connected);
    void outputSensorConnectionChanged(boolean connected);
    void speedSensorHasExceedMinThreshold(boolean isOutOfRange);
    void speedSensorHasExceedMaxThreshold(boolean isOutOfRange);
}
