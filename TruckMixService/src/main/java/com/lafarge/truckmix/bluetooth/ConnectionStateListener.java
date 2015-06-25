package com.lafarge.truckmix.bluetooth;

public interface ConnectionStateListener {

    /**
     * Called when TruckMix is connected to the calculator.
     */
    void onCalculatorConnected();

    /**
     * Called when TruckMix is connecting to the calculator.
     */
    void onCalculatorConnecting();

    /**
     * Called when TruckMix has lost connection with the calculator.
     */
    void onCalculatorDisconnected();
}
