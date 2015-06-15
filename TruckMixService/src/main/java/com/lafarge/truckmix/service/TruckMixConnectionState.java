package com.lafarge.truckmix.service;

public interface TruckMixConnectionState {

    void onCalculatorConnected();
    void onCalculatorConnecting();
    void onCalculatorDisconnected();
}
