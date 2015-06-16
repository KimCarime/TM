# TruckMix

[![Build Status](https://magnum.travis-ci.com/IntactProjects/TruckMix.svg?token=3qDVy8NqsczQXKzDk5VQ&branch=develop)](https://magnum.travis-ci.com/IntactProjects/TruckMix)

# Purpose

TruckMix is an embedded system on the mixer truck that will give information on concrete during transportation. The
device is able to communicate with other system and share additional data on the delivery.

# Overview

The project is separated into three sub-projects. The first one, TruckMix, is a java library that contains all
business logic about the communication with the device.
The second one, TruckMixService, is an Android service that wrap the first library. Its job is to communicate with
the device through bluetooth and to expose an API to communicate with it.
The last project, TruckMixDemo, is a demo project to show how to integrate TruckMixService and how use the API.

# Integration

Add these permissions in `AndroidManifest.xml`:

    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.BLUETOOTH" />

Then declare the service as follow :<br>

    <service
        android:name="com.lafarge.truckmix.service.TruckMixService"
        android:exported="false"
        android:label="TruckMix Service"
        android:process=":TruckMix">
    </service>
