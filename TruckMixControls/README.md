# TruckMix Controls

## Overview
This component contains controls for TruckMix. Currently there is only a Slumpometer Gauge.

## Usage

Import the library to your project.

In your layout xml-file add SlumpometerGauge as shown:

```xml
<com.lafarge.truckmix.controls.SlumpometerGauge
    android:layout_height="wrap_content"
    android:layout_width="match_parent"
    android:padding="8dp"
    android:id="@+id/slumpometer" />
```

Configure SlumpometerGauge:

```java
private SlumpometerGauge slumpometer;

// Customize SpeedometerGauge
slumpometer = (SlumpometerGauge) v.findViewById(R.id.slumpometer);

// configure concrete range, tolerance and type
speedometer.setConcreteRange(100, 150);
speedometer.setTolerance(10);
speedometer.setConcreteCode("S3");
```