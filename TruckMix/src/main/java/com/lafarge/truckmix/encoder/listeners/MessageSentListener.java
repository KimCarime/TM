package com.lafarge.truckmix.encoder.listeners;

import com.lafarge.truckmix.common.enums.CommandPumpMode;

/**
 * This interface list every messages that the Encoder have actually encoded.
 */
public interface MessageSentListener {

    void targetSlump(int value, byte[] bytes);
    void maximumWater(int value, byte[] bytes);
    void waterAdditionPermission(boolean isAllowed, byte[] bytes);
    void changeExternalDisplayState(boolean isActivated, byte[] bytes);
    void endOfDelivery(byte[] bytes);
    void beginningOfDelivery(byte[] bytes);
    void loadVolume(double value, byte[] bytes);
    void parameterT1(double value, byte[] bytes);
    void parameterA11(double value, byte[] bytes);
    void parameterA12(double value, byte[] bytes);
    void parameterA13(double value, byte[] bytes);
    void magnetQuantity(int value, byte[] bytes);
    void timePump(int value, byte[] bytes);
    void timeDelayDriver(int value, byte[] bytes);
    void pulseNumber(int value, byte[] bytes);
    void flowmeterFrequency(int value, byte[] bytes);
    void commandPumpMode(CommandPumpMode commandPumpMode, byte[] bytes);
    void calibrationInputSensorA(double value, byte[] bytes);
    void calibrationOutputSensorA(double value, byte[] bytes);
    void calibrationInputSensorB(double value, byte[] bytes);
    void calibrationOutputSensorB(double value, byte[] bytes);
    void openingTimeEV1(int value, byte[] bytes);
    void openingTimeVA1(int value, byte[] bytes);
    void countingTolerance(int value, byte[] bytes);
    void waitingDurationAfterWaterAddition(int value, byte[] bytes);
    void maxDelayBeforeFlowage(int value, byte[] bytes);
    void maxFlowageError(int value, byte[] bytes);
    void maxCountingError(int value, byte[] bytes);
    void fake(byte[] bytes);
}
