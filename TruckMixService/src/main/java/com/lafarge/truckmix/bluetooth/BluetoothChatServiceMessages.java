package com.lafarge.truckmix.bluetooth;

import android.os.Bundle;
import android.os.Message;

/**
 * Defines several constants of the BluetoothChatService.
 */
public class BluetoothChatServiceMessages {
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_BLUETOOTH_STATE_OFF = 4;
    public static final int MESSAGE_BLUETOOTH_STATE_ON = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String KEY_MSG_DEVICE_CONNECTED_NAME = "device_name";
    public static final String KEY_MSG_DEVICE_CONNECTED_ADDRESS = "device_address";

    //
    // List of messages
    //

    public static Message createDeviceConnectingMessage(String name, String address) {
        Message msg = Message.obtain(null, MESSAGE_STATE_CHANGE, BluetoothChatService.STATE_CONNECTING, -1);
        Bundle data = new Bundle();
        data.putString(KEY_MSG_DEVICE_CONNECTED_NAME, name);
        data.putString(KEY_MSG_DEVICE_CONNECTED_ADDRESS, address);
        msg.setData(data);
        return msg;
    }

    public static Message createDeviceConnectedMessage(String name, String address) {
        Message msg = Message.obtain(null, MESSAGE_STATE_CHANGE, BluetoothChatService.STATE_CONNECTED, -1);
        Bundle data = new Bundle();
        data.putString(KEY_MSG_DEVICE_CONNECTED_NAME, name);
        data.putString(KEY_MSG_DEVICE_CONNECTED_ADDRESS, address);
        msg.setData(data);
        return msg;
    }

    public static Message createBluetoothStateOffMessage() {
        return Message.obtain(null, MESSAGE_BLUETOOTH_STATE_OFF);
    }

    public static Message createBluetoothStateOnMessage() {
        return Message.obtain(null, MESSAGE_BLUETOOTH_STATE_ON);
    }

    //
    // List of getter
    //
    public static BluetoothDeviceInfo getDeviceFromDeviceConnectedMessage(Message msg) {
        String address = msg.getData().getString(KEY_MSG_DEVICE_CONNECTED_NAME);
        String name = msg.getData().getString(KEY_MSG_DEVICE_CONNECTED_ADDRESS);
        return new BluetoothDeviceInfo(name, address);
    }


    public static class BluetoothDeviceInfo {
        private final String name;
        private final String address;

        public BluetoothDeviceInfo(String name, String address) {
            this.name = name;
            this.address = address;
        }

        public String getName() {
            return this.name;
        }

        public String getAddress() {
            return this.address;
        }
    }
}