package com.lafarge.truckmix;

public interface TruckMixConnectionState {

    void onCalculatorConnected();
    void onCalculatorConnecting();
    void onCalculatorDisconnected();
}
