# TruckMix Library

## Overview

This component contains the communication protocol and the business logic of TruckMix.
It allow client to chat with the calculator at a low level.

## How it works

The calculator send bytes to be interpreted and the `Communicator` class send bytes to the calculator.
The `Communicator` received bytes to decode through its method `received(byte[] bytes)` and tell what to send with its listener `CommunicatorBytesListener`.
Once a message was decoded from the calculator, its interpretation is send via the `CommunicatorListener`.
It is to the user to send correct response according to the context via public methods of the communicator.

## How to build this Library

```bash
./gradlew test # run unit tests
./gradlew build # development build
./gradlew release # release build
```

## Usage

The `Communicator` class is the input/output point of the the library. You should not use other classes.
```java
// All parameters are mandatory.
Communicator communicator = new Communicator(mCommunicatorBytesListener, mCommunicatorListener, mLoggerListener, mEventListener);

// Tell the communicator that you are connected. This is generally used by the top layer of the communicator.
communicator.setConnected(true);
```

To configure options of the communicator:
```java
// Quality tracking activation (sent via EventListener).
communicator.setQualityTrackingEnabled(boolean true);

// Enable/Disable water request, useful for country where it is forbidden.
communicator.setWaterRequestAllowed(boolean true);
```

To send send truck parameters:
```java
communicator.setTruckParameters(new TruckParameters(...));
```

To pass a delivery note:
```java
communicator.deliveryNoteReceived(new DeliveryParameters(...));
```

To start and stop a delivery:
```java
communicator.acceptDelivery(true);
communicator.endDelivery();
```

For more information, see javadoc.
