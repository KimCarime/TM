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
import android.util.Log;

import com.lafarge.truckmix.communicator.listeners.LoggerListener;
import com.lafarge.truckmix.service.TruckMixService;
import com.lafarge.truckmix.utils.Convert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
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
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final TruckMixService.TruckMixContext mContext;
    private final BluetoothAdapter mBtAdapter;
    private ConnectionThread mConnectionThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private Timer mTimer;
    private boolean mStopped;
    private String mDeviceAddress;

    private final LoggerListener mLoggerListener;
    private final ConnectionStateListener mConnectionStateListener;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 2;  // now connected to a remote device

    // Constant that indicate the delay before retrying a connection
    public static final int RETRY_CONNECTION_DELAY_IN_MILLIS = 10 * 1000;

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context TruckMixContext
     * @param connectionStateListener listener for conntection state
     * @param loggerListener listener for logs
     */
    public BluetoothChatService(
            TruckMixService.TruckMixContext context,
            ConnectionStateListener connectionStateListener,
            LoggerListener loggerListener) {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;

        mContext = context;
        mConnectionStateListener = connectionStateListener;
        mLoggerListener = loggerListener;

        if (mBtAdapter == null) {
            Log.e(TAG, "Bluetooth is not supported on this device");
        } else {
            if (!mBtAdapter.isEnabled()) {
                mBtAdapter.enable();
            }
            mContext.getServiceInstance().registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        }
    }

    /**
     * Start the ConnectionThread to initiate a connection to a remote device.
     *
     * @param address The address of the remote device to connect.
     * @throws IllegalArgumentException If address is not a valid bluetooth mac address.
     */
    public synchronized void connect(final String address) {
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
        Log.d(TAG, "will connect to: " + device);

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

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, device);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void disconnect() {
        Log.d(TAG, "disconnect");
        mStopped = true;
        mDeviceAddress = null;

        cancelTimer();
        cancelThreads();

        setState(STATE_NONE);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");
        disconnect();
        if (mBtAdapter != null) {
            mContext.getServiceInstance().unregisterReceiver(mReceiver);
        }
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
     */
    private void retryConnection(final BluetoothDevice device) {
        if (mStopped) return;

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
        cancelThreads();

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
        switch (state) {
            case STATE_NONE:
                mLoggerListener.log("BLUETOOTH: disconnected");
                mConnectionStateListener.onCalculatorDisconnected();
                mContext.getCommunicatorInstance().setConnected(false);
                break;
            case STATE_CONNECTING:
                mLoggerListener.log("BLUETOOTH: connecting to " + mDeviceAddress);
                mConnectionStateListener.onCalculatorConnecting();
                break;
            case STATE_CONNECTED:
                mLoggerListener.log("BLUETOOTH: connected");
                mContext.getCommunicatorInstance().setConnected(true);
                mConnectionStateListener.onCalculatorConnected();
                break;
        }
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
                retryConnection(mmDevice);
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
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    final byte[] finalBuff = Arrays.copyOf(buffer, bytes);

                    mLoggerListener.log("BLUETOOTH: received " + Convert.bytesToHex(finalBuff));
                    mContext.getCommunicatorInstance().received(finalBuff);
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    retryConnection(mmDevice);
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(final byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mLoggerListener.log("BLUETOOTH: sent " + Convert.bytesToHex(buffer));
            } catch (IOException e) {
                // Connection was lost
                Log.e(TAG, "Exception during write", e);
                connectionLost();
                retryConnection(mmDevice);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
                mmInStream.close();
                mmOutStream.close();
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
                    mLoggerListener.log("BLUETOOTH: bluetooth was turned off -> re-enable it");

                    // Cancel any thread and timer
                    cancelTimer();
                    cancelThreads();

                    // Re-enable bluetooth
                    mBtAdapter.enable();

                } else if (mBtAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    mLoggerListener.log("BLUETOOTH: bluetooth is on");
                    if (mDeviceAddress != null) {
                        connect(mDeviceAddress);
                    }
                }
            }
        }
    };
}
