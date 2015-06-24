# TruckMix

[![Build Status](https://magnum.travis-ci.com/IntactProjects/TruckMix.svg?token=3qDVy8NqsczQXKzDk5VQ&branch=develop)](https://magnum.travis-ci.com/IntactProjects/TruckMix)

## Purpose

TruckMix is an embedded system on the mixer truck that will give information on concrete during transportation. The
device is able to communicate with other system and share additional data on the delivery.

## Overview

The project is separated into three sub-projects. The first one, TruckMix, is a java library that contains all
business logic about the communication with the device.
The second one, TruckMixService, is an Android service that wrap the first library. Its job is to communicate with
the device through bluetooth and to expose an API to communicate with it.
The last project, TruckMixDemo, is a demo project to show how to integrate TruckMixService and how use the API.

## Integration

### Android Studio / Gradle

Step 1:

Add the library ARR in your `libs` folder.

Step 2:

Add the library as a dependency like so : 

    repositories {
        flatDir{
            dirs 'libs'
        }
    }

    dependencies {
        compile 'name: TruckMix', ext: 'aar')
    }

### Eclipse

Step 1:

Import the library to Eclipse

Step 2:

Add these permissions in `AndroidManifest.xml`:

    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.BLUETOOTH" />

Step 3:

Edit your `project.properties` file and add the line: 
    
    manifest.enable=true

## TODO

- Test TruckMixService integration
- Add Proguard
- Merge TruckMix and TruckMixService into a single library (merge projects or uber jar)
- Enhance logging strategy

