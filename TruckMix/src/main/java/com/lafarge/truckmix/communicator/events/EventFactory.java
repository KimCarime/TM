package com.lafarge.truckmix.communicator.events;

import com.lafarge.truckmix.common.enums.RotationDirection;

/**
 * Factory of Event, used by the Communicator to send events to EventListener.
 *
 * @see Event
 * @see com.lafarge.truckmix.communicator.listeners.EventListener
 */
public class EventFactory {

    public static Event createStartDeliveryEvent(boolean accepted) {
        return new Event<Integer>(Event.EventId.START_DELIVERY, accepted ? 1 : 0);
    }

    public static Event createEndOfDeliveryEvent(Integer currentSlump) {
        return new Event<Integer>(Event.EventId.END_DELIVERY, currentSlump);
    }

    public static Event createBluetoothConnectionEvent(boolean isConnected) {
        return new Event<Integer>(Event.EventId.CONNECTION_BLUETOOTH, isConnected ? 1 : 0);
    }

    public static Event createNewSlumpEvent(int slump) {
        return new Event<Integer>(Event.EventId.NEW_SLUMP, slump);
    }

    public static Event createNewTemperatureEvent(float temperature) {
        return new Event<Float>(Event.EventId.NEW_TEMPERATURE, temperature);
    }

    public static Event createMixerTransitionEvent(RotationDirection rotationDirection) {
        switch (rotationDirection) {
            case MIXING:
                return new Event<Integer>(Event.EventId.MIXER_TRANSITION, 0);
            case UNLOADING:
                return new Event<Integer>(Event.EventId.MIXER_TRANSITION, 1);
            default:
                throw new IllegalArgumentException("Event for this RotationDirection is not yet supported");
        }
    }

    public static Event createInputPressureEvent(float inputPressure) {
        return new Event<Float>(Event.EventId.INPUT_PRESSURE, inputPressure);
    }

    public static Event createOutputPressureEvent(float outputPressure) {
        return new Event<Float>(Event.EventId.OUTPUT_PRESSURE, outputPressure);
    }

    public static Event createRotationSpeedEvent(float rotationSpeed) {
        return new Event<Float>(Event.EventId.ROTATION_SPEED, rotationSpeed);
    }

    public static Event createRotationSpeedLimitMinEvent(boolean thresholdExceed) {
        return new Event<Integer>(Event.EventId.ROTATION_SPEED_LIMIT, thresholdExceed ? 2 : 4);
    }

    public static Event createRotationSpeedLimitMaxEvent(boolean thresholdExceed) {
        return new Event<Integer>(Event.EventId.ROTATION_SPEED_LIMIT, thresholdExceed ? 1 : 3);
    }
}
