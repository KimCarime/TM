package com.lafarge.tm;

public interface CommunicatorListener {

    /**
     *  Method triggered when a message should be send
     *
     *  @param bytes The bytes to send
     */
    void send(byte[] bytes);

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

    boolean bullshitToDel();
}
