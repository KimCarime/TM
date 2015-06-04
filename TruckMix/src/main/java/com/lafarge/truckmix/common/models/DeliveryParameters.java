package com.lafarge.truckmix.common.models;

public class DeliveryParameters {

    public final int targetSlump;
    public final int maxWater;
    public final int loadVolume;

    public DeliveryParameters(int targetSlump, int maxWater, int loadVolume) {
        this.targetSlump = targetSlump;
        this.maxWater = maxWater;
        this.loadVolume = loadVolume;
    }
}
