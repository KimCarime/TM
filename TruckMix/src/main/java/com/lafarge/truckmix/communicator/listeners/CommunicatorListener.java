package com.lafarge.truckmix.communicator.listeners;

import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;

public interface CommunicatorListener {

    void slumpUpdated(int slump);
    void mixingModeActivated();
    void unloadingModeActivated();
    void waterAdded(int volume, MessageReceivedListener.WaterAdditionMode additionMode);
    void waterAdditionRequest(int volume);
    void waterAdditionBegan();
    void waterAdditionEnd();
    void alarmWaterAdditionBlocked();
    void stateChanged(int step, int subStep);
    void calibrationData(float inPressure, float outPressure, float rotationSpeed);
    void alarmWaterMax();
    void alarmFlowageError();
    void alarmCountingError();
    void inputSensorConnectionChanged(boolean connected);
    void outputSensorConnectionChanged(boolean connected);
    void speedSensorHasExceedMinThreshold(boolean isOutOfRange);
    void speedSensorHasExceedMaxThreshold(boolean isOutOfRange);
}
