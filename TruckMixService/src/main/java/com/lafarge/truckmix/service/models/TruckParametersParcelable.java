package com.lafarge.truckmix.service.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.lafarge.truckmix.common.models.TruckParameters;

public class TruckParametersParcelable extends TruckParameters implements Parcelable {

    public TruckParametersParcelable(double T1, double A11, double A12, double A13, int magnetQuantity, int timePump, int timeDelayDriver, int pulseNumber, int flowmeterFrequency, CommandPumpMode commandPumpMode, double calibrationInputSensorA, double calibrationInputSensorB, double calibrationOutputSensorA, double calibrationOutputSensorB, int openingTimeEV1, int openingTimeVA1, int toleranceCounting, int waitingDurationAfterWaterAddition, int maxDelayBeforeFlowage, int maxFlowageError, int maxCountingError) {
        super(T1, A11, A12, A13, magnetQuantity, timePump, timeDelayDriver, pulseNumber, flowmeterFrequency, commandPumpMode, calibrationInputSensorA, calibrationInputSensorB, calibrationOutputSensorA, calibrationOutputSensorB, openingTimeEV1, openingTimeVA1, toleranceCounting, waitingDurationAfterWaterAddition, maxDelayBeforeFlowage, maxFlowageError, maxCountingError);
    }

    public TruckParametersParcelable(TruckParameters parameters) {
        super(parameters.T1, parameters.A11, parameters.A12, parameters.A13, parameters.magnetQuantity, parameters.timePump, parameters.timeDelayDriver, parameters.pulseNumber, parameters.flowmeterFrequency, parameters.commandPumpMode, parameters.calibrationInputSensorA, parameters.calibrationInputSensorB, parameters.calibrationOutputSensorA, parameters.calibrationOutputSensorB, parameters.openingTimeEV1, parameters.openingTimeVA1, parameters.toleranceCounting, parameters.waitingDurationAfterWaterAddition, parameters.maxDelayBeforeFlowage, parameters.maxFlowageError, parameters.maxCountingError);
    }

    protected TruckParametersParcelable(Parcel in) {
        super(in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), (CommandPumpMode) in.readValue(CommandPumpMode.class.getClassLoader()), in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(T1);
        dest.writeDouble(A11);
        dest.writeDouble(A12);
        dest.writeDouble(A13);
        dest.writeInt(magnetQuantity);
        dest.writeInt(timePump);
        dest.writeInt(timeDelayDriver);
        dest.writeInt(pulseNumber);
        dest.writeInt(flowmeterFrequency);
        dest.writeValue(commandPumpMode);
        dest.writeDouble(calibrationInputSensorA);
        dest.writeDouble(calibrationInputSensorB);
        dest.writeDouble(calibrationOutputSensorA);
        dest.writeDouble(calibrationOutputSensorB);
        dest.writeInt(openingTimeEV1);
        dest.writeInt(openingTimeVA1);
        dest.writeInt(toleranceCounting);
        dest.writeInt(waitingDurationAfterWaterAddition);
        dest.writeInt(maxDelayBeforeFlowage);
        dest.writeInt(maxFlowageError);
        dest.writeInt(maxCountingError);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TruckParameters> CREATOR = new Parcelable.Creator<TruckParameters>() {
        @Override
        public TruckParameters createFromParcel(Parcel in) {
            return new TruckParametersParcelable(in);
        }

        @Override
        public TruckParameters[] newArray(int size) {
            return new TruckParameters[size];
        }
    };
}
