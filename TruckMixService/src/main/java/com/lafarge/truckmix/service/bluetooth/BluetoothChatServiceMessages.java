package com.lafarge.truckmix.service.bluetooth;

/**
 * Defines several constants of the BluetoothChatService.
 */
public class BluetoothChatServiceMessages {
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
}