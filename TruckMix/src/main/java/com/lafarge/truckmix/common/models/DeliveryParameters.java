package com.lafarge.truckmix.common.models;

/**
 * This class contains all information that the calculator need for a delivery.
 */
public class DeliveryParameters {

    // Member fields
    public final int targetSlump;
    public final int maxWater;
    public final int loadVolume;

    /**
     * Constructor
     *
     * @param targetSlump The target slump
     * @param maxWater The max water
     * @param loadVolume The volume of concrete
     */
    public DeliveryParameters(int targetSlump, int maxWater, int loadVolume) {
        this.targetSlump = targetSlump;
        this.maxWater = maxWater;
        this.loadVolume = loadVolume;
    }
}
