/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lafarge.truckmix.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothChatService {
    // Debugging
    private static final String TAG = "BluetoothChatService";

    // Unique UUID for this application
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final Context mContext;
    private final BluetoothAdapter mBtAdapter;
    private final Handler mHandler;
    private ConnectionThread mConnectionThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private Timer mTimer;
    private boolean mStopped;
    private String mDeviceAddress;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 2;  // now connected to a remote device

    // Constant that indicate the delay before retrying a connection
    public static final int RETRY_CONNECTION_DELAY_IN_MILLIS = 10 * 1000;

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context A context to use to register
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothChatService(Context context, Handler handler) {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mContext = context;
        mHandler = handler;

        if (mBtAdapter == null) {
            Log.e(TAG, "Bluetooth is not supported on this device");
        } else {
            if (!mBtAdapter.isEnabled()) {
                mBtAdapter.enable();
            }
            context.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        }
    }

    /**
     * Start the ConnectionThread to initiate a connection to a remote device.
     *
     * @param address The address of the remote device to connect
     */
    public synchronized void connect(String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            throw new IllegalArgumentException("Address: " + address + " is not valid");
        }

        if (mBtAdapter == null) {
            Log.e(TAG, "Can't connect because bluetooth is not supported by this platform");
            return;
        }

        // Cancel current connection or reconnection
        mStopped = true;
        cancelTimer();
        cancelThreads();

        // Convert mac address in BluetoothDevice
        BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        Log.d(TAG, "connect to: " + device);

        // Keep address
        mDeviceAddress = address;

        // Start the thread to connect with the given device
        mStopped = false;
        mConnectionThread = new ConnectionThread(device);
        mConnectionThread.start();

        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");
        mStopped = false;

        // Cancel the thread that completed the connection and any thread currently running a connection
        cancelTimer();
        cancelThreads();

        setState(STATE_CONNECTED);

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, device);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(BluetoothChatServiceMessages.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChatServiceMessages.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");
        mStopped = true;
        mContext.unregisterReceiver(mReceiver);

        cancelTimer();
        cancelThreads();

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) {
                return;
            }
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    private void cancelThreads() {
        // Cancel any thread attempting to make a connection
        if (mConnectionThread != null) {
            mConnectionThread.cancel();
            mConnectionThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
    }


    /**
     * Reinitialize threads and retry a connection.
     *
     * @param device The device to reconnect
     *
     */
    private void retryConnection(final BluetoothDevice device) {
        Log.i(TAG, "Retry connection with device " + device);

        // Cancel threads and timer, just to be sure...
        cancelTimer();
        cancelThreads();

        // Schedule a connection retry
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Cancel any connection that could be established during the delay of retry
                cancelTimer();
                cancelThreads();

                mConnectionThread = new ConnectionThread(device);
                mConnectionThread.start();
            }
        };
        mTimer = new Timer();
        mTimer.schedule(task, RETRY_CONNECTION_DELAY_IN_MILLIS);
    }

    /**
     * Indicate that the connection was lost.
     */
    private void connectionLost() {
        // Start the service over to restart listening mode
        if (mConnectionThread != null) {
            mConnectionThread.cancel();
            mConnectionThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(BluetoothChatServiceMessages.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectionThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectionThread(BluetoothDevice device) {
            setName("ConnectionThread");
            mmDevice = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
//                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectionThread");

            // Always cancel discovery because it will slow down a connection
            mBtAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                Log.e(TAG, "unable to connect() with device " + mmDevice);
                // Close the socket
                if (!mStopped) {
                    retryConnection(mmDevice);
                }
                return;
            }

            // Reset the ConnectionThread because we're done
            synchronized (BluetoothChatService.this) {
                mConnectionThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothDevice mmDevice;
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, BluetoothDevice device) {
            setName("ConnectedThread");
            Log.d(TAG, "create ConnectedThread");
            mmDevice = device;
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(BluetoothChatServiceMessages.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    if (!mStopped) {
                        retryConnection(mmDevice);
                    }
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(BluetoothChatServiceMessages.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                // Connection was lost
                Log.e(TAG, "Exception during write", e);
                connectionLost();
                if (!mStopped) {
                    retryConnection(mmDevice);
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                if (mBtAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    Log.i(TAG, "Bluetooth is disabled -> Enabling it");

                    // Cancel any thread and timer
                    cancelTimer();
                    cancelThreads();

                    // Re-enable bluetooth
                    mBtAdapter.enable();

                } else if (mBtAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    Log.i(TAG, "Bluetooth is on");
                    if (mDeviceAddress != null) {
                        connect(mDeviceAddress);
                    }
                }
            }
        }
    };
}
