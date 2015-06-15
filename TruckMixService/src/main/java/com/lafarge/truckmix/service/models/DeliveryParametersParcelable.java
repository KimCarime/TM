package com.lafarge.truckmix.service.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.lafarge.truckmix.common.models.DeliveryParameters;

public class DeliveryParametersParcelable extends DeliveryParameters implements Parcelable {

    public DeliveryParametersParcelable(DeliveryParameters parameters) {
        super(parameters.targetSlump, parameters.maxWater, parameters.loadVolume);
    }

    public DeliveryParametersParcelable(Parcel in) {
        super(in.readInt(), in.readInt(), in.readInt());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(targetSlump);
        dest.writeInt(maxWater);
        dest.writeInt(loadVolume);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<DeliveryParameters> CREATOR = new Parcelable.Creator<DeliveryParameters>() {
        @Override
        public DeliveryParameters createFromParcel(Parcel in) {
            return new DeliveryParametersParcelable(in);
        }

        @Override
        public DeliveryParameters[] newArray(int size) {
            return new DeliveryParameters[size];
        }
    };
}
