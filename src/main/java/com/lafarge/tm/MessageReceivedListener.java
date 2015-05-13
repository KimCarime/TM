package com.lafarge.tm;

public interface MessageReceivedListener {

    public enum WaterAddition {manual, auto}

    void slumpUpdated(int slump);
    void waterAdded(int volume, WaterAddition waterAddition);

}
