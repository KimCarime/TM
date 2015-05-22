package com.lafarge.tm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GroupEncoder extends Encoder {

    public byte[] truckParameters(TruckParameters parameters) {
        assert parameters != null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            out.write(this.parameterT1(parameters.T1));
            out.write(this.parameterA11(parameters.A11));
            out.write(this.parameterA12(parameters.A12));
            out.write(this.parameterA13(parameters.A13));
            out.write(this.magnetQuantity(parameters.magnetQuantity));
            out.write(this.timePump(parameters.timePump));
            out.write(this.timeDelayDriver(parameters.timeDelayDriver));
            out.write(this.pulseNumber(parameters.pulseNumber));
            out.write(this.debimeterFrequency(parameters.flowmeterFrequency));
            out.write(this.commandPumpMode(parameters.commandPumpMode));
            out.write(this.calibrationInputSensorA(parameters.calibrationInputSensorA));
            out.write(this.calibrationInputSensorB(parameters.calibrationInputSensorB));
            out.write(this.calibrationOutputSensorA(parameters.calibrationOutputSensorA));
            out.write(this.calibrationOutputSensorB(parameters.calibrationOutputSensorB));
            out.write(this.openingTimeEV1(parameters.openingTimeEV1));
            out.write(this.openingTimeVA1(parameters.openingTimeVA1));
            out.write(this.countingTolerance(parameters.toleranceCounting));
            out.write(this.waitingDurationAfterWaterAddition(parameters.waitingDurationAfterWaterAddition));
            out.write(this.maxDelayBeforeFlowage(parameters.maxDelayBeforeFlowage));
            out.write(this.maxFlowageError(parameters.maxFlowageError));
            out.write(this.maxCountingError(parameters.maxCountingError));
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] deliveryParameters(DeliveryParameters parameters) {
        assert parameters != null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            out.write(this.targetSlump(parameters.targetSlump));
            out.write(this.maximumWater(parameters.maxWater));
            out.write(this.loadVolume(parameters.loadVolume));
            return out.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
}
