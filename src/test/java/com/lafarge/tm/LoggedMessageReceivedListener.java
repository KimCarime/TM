package com.lafarge.tm;

public class LoggedMessageReceivedListener implements MessageReceivedListener {
    @Override
    public void slumpUpdated(int slump) {
        System.out.println("  -> slump updated: " + slump);
    }

    @Override
    public void mixingModeActivated() {
        System.out.println("  -> mixing mode activated");
    }

    @Override
    public void unloadingModeActivated() {
        System.out.println("  -> unloading mode activated");
    }

    @Override
    public void waterAdded(int volume, WaterAdditionMode additionMode) {
        System.out.println("  -> water added (volume: " + volume + ", additionMode: " + additionMode.toString() + ")");
    }

    @Override
    public void waterAdditionRequest(int volume) {
        System.out.println("  -> request water addition: " + volume);
    }

    @Override
    public void waterAdditionBegan() {
        System.out.println("  -> water addition began");
    }

    @Override
    public void waterAdditionEnd() {
        System.out.println("  -> water addition end");
    }

    @Override
    public void alarmWaterAdditionBlocked() {
        System.out.println("  -> alarm: water addition blocked");
    }

    @Override
    public void truckParametersRequest() {
        System.out.println("  -> truck parameters request");
    }

    @Override
    public void truckParametersReceived() {
        System.out.println("  -> truck parameters received");
    }

    @Override
    public void deliveryParametersRequest() {
        System.out.println("  -> delivery parameters request");
    }

    @Override
    public void deliveryParametersReceived() {
        System.out.println("  -> delivery parameters received");
    }

    @Override
    public void deliveryValidationRequest() {
        System.out.println("  -> delivery validation request");
    }

    @Override
    public void deliveryValidationReceived() {
        System.out.println("  -> delivery validation received");
    }

    @Override
    public void stateChanged(int step, int subStep) {
        System.out.println("  -> state changed (step: " + step + ", subStep: " + subStep);
    }

    @Override
    public void traceDebug(String trace) {
        System.out.println("  -> trace Debug: " + trace);
    }

    @Override
    public void rawData(int inPressure, int outPressure, int interval, boolean buttonHold) {
        System.out.println("  -> raw Data (inPressure: " + inPressure + ", outPressure:" + outPressure + ", interval: " + interval + ", buttonHold: " + (buttonHold ? "YES" : "NO"));
    }

    @Override
    public void derivedData(RotationDirection rotationDirection, boolean slumpFrameStable, int currentFrameSize, int expectedFrameSize) {
        System.out.println("  -> derived Data (rotationDirection: " + rotationDirection.toString() + ", stable: " + (slumpFrameStable ? "YES" : "NO") + ", currentFrameSize: " + currentFrameSize + ", expectedFrameSize: " + expectedFrameSize);
    }

    @Override
    public void internData(boolean inSensorConnected, boolean outSensorConnected, boolean speedTooLow, boolean speedTooHigh, boolean commandEP1Activated, boolean commandVA1Activated) {
        System.out.println("  -> intern Data (inSensorConnected: " + (inSensorConnected ? "YES" : "NO") + ", outSensorConnected: " + (outSensorConnected ? "YES" : "NO") + ", speedTooLow: " + (speedTooLow ? "YES" : "NO") + ", speedTooHigh: " + (speedTooHigh ? "YES" : "NO") + ", commandEP1Activated: " + (commandEP1Activated ? "YES" : "NO") + ", commandVA1Activated: " + (commandVA1Activated ? "YES" : "NO"));
    }

    @Override
    public void calibrationData(float inPressure, float outPressure, float rotationSpeed) {
        System.out.println("  -> calibration Data (inPressure: " + inPressure + ", outPressure:" + outPressure + ", rotationSpeed: " + rotationSpeed);
    }

    @Override
    public void alarmWaterMax() {
        System.out.println("  -> alarm: water max");
    }

    @Override
    public void alarmFlowageError() {
        System.out.println("  -> alarm: flowage error");
    }

    @Override
    public void alarmCountingError() {
        System.out.println("  -> alarm: counting error");
    }

    @Override
    public void inputSensorConnectionChanged(boolean connected) {
        System.out.println("  -> input sensor connection changed: " + (connected ? "CONNECTED" : "NOT CONNECTED"));
    }

    @Override
    public void outputSensorConnectionChanged(boolean connected) {
        System.out.println("  -> output sensor connection changed: " + (connected ? "CONNECTED" : "NOT CONNECTED"));
    }

    @Override
    public void speedSensorHasExceedMinThreshold(boolean isOutOfRange) {
        System.out.println("  -> speed sensor exceed min threshold: " + (isOutOfRange ? "NO" : "YES(out of range)"));
    }

    @Override
    public void speedSensorHasExceedMaxThreshold(boolean isOutOfRange) {
        System.out.println("  -> speed sensor exceed max threshold: " + (isOutOfRange ? "NO" : "YES(out of range)"));
    }
}
