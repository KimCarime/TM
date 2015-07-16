# TruckMix

[![Build Status](https://magnum.travis-ci.com/IntactProjects/TruckMix.svg?token=3qDVy8NqsczQXKzDk5VQ&branch=develop)](https://magnum.travis-ci.com/IntactProjects/TruckMix)

## Purpose

TruckMix is an embedded system on the mixer truck that will give information on concrete during transportation. The
device is able to communicate with other system and share additional data on the delivery.

## Overview

The project is separated into four sub-projects. The first one, [TruckMix](TruckMix/README.md), is a java library that contains all
business logic about the communication with the device.
The second one, [TruckMixService](TruckMixService/README.md), is an Android service that wrap the first library. Its job is to communicate with
the device through bluetooth and to expose an API to communicate with it.
The third one, [TruckMixControls](TruckMixControls/README.md), contains controls for TruckMix. Currently there is only a gauge for the slump called SlumpometerGauge.
The last project, [TruckMixDemo](TruckMixDemo/README.md), is a demo project to show how to integrate TruckMixService and how use the API.

## TODO

- Test TruckMixService integration
- Test TruckMixControls integration
- Add Proguard
- Merge TruckMix and TruckMixService into a single library (merge projects)
- Enhance logging strategy
- Add xxhdpi, etc for TruckMixService drawables, and more default languages for strings

