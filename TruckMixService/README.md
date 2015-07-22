# TruckMix Service

## Overview
This component wrap the TruckMix library ([see here](TruckMix/README.md)) and provide an API to use it on Android.
The service also manage the communication with the calculator through bluetooth.

## How to build this library

```bash
./gradlew test # run unit tests
./gradlew build # development build
./gradlew release # release build
```

## Usage

To create a `TruckMix` client, you have to use its builder via `TruckMix.Builder` with specifies which implementations to use for each of the various abstraction interfaces.

```java
TruckMix truckmix = new TruckMix.Builder(context)
                .setNotificationActivated(true, pendingIntent)
                .setConnectionStateListener(connectionStateListener)
                .setCommunicatorListener(communicatorListener)
                .setLoggerListener(loggerListener)
                .setEventListener(eventListener)
                .build();
```

Note that parameters are not mandatory, if you don't want for example logs, you don't have to set it to null.

## Customization

If you activated the notification, `TruckMixService` comes with default icons and labels, which you can change by overwriting them.
The list of them is as follow :

```
res/
    drawable-hdpi/
        truckmix_status_icon_disconnected.png
        truckmix_status_icon_disconnected.png
    drawable-mdpi/
        truckmix_status_icon_disconnected.png
        truckmix_status_icon_disconnected.png
    drawable-xhdpi/
        truckmix_status_icon_disconnected.png
        truckmix_status_icon_disconnected.png
```
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="truckmix_notif_title">TruckMix</string>
    <string name="truckmix_notif_connected">TruckMix: Truck connected</string>
    <string name="truckmix_notif_disconnected">TruckMix: Truck disconnected</string>
</resources>
```