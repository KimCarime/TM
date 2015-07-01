package com.lafarge.truckmix.communicator.listeners;

import com.lafarge.truckmix.common.enums.AlarmType;
import com.lafarge.truckmix.common.enums.RotationDirection;
import com.lafarge.truckmix.common.enums.SpeedSensorState;
import com.lafarge.truckmix.common.enums.WaterAdditionMode;

/**
 * Interface of message received from the calculator.
 */
public interface CommunicatorListener {

    void slumpUpdated(final int slump);
    void temperatureUpdated(final float temperature);
    void rotationDirectionChanged(final RotationDirection rotationDirection);
    void waterAdded(final int volume, final WaterAdditionMode additionMode);
    void waterAdditionRequest(final int volume);
    void waterAdditionBegan();
    void waterAdditionEnd();
    void stateChanged(final int step, final int subStep);
    void internData(final boolean inputSensorConnected, final boolean outputSensorConnected, final SpeedSensorState speedSensorState);
    void calibrationData(float inputPressure, float outputPressure, final float rotationSpeed);
    void inputSensorConnectionChanged(final boolean connected);
    void outputSensorConnectionChanged(final boolean connected);
    void speedSensorStateChanged(final SpeedSensorState speedSensorState);
    void alarmTriggered(final AlarmType alarmType);
}
