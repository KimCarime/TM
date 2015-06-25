package com.lafarge.truckmix.communicator.events;

/**
 * Event
 *
 * @param <T> The type of the event value
 */
public class Event<T> {

    public enum EventId {
        UNKNOWN(-1),
        START_DELIVERY(1),
        END_DELIVERY(2),
        NEW_SLUMP(3),
        MIXER_TRANSITION(4),
        NEW_TEMPERATURE(5),
        ROTATION_SPEED_LIMIT(6),
        ROTATION_SPEED(7),
        INPUT_PRESSURE(8),
        OUTPUT_PRESSURE(9),
        CONNECTION_BLUETOOTH(10);

        private final int idValue;

        EventId(int idValue) {
            this.idValue = idValue;
        }

        public static EventId getEnum(int idValue) {
            for (EventId eventId : EventId.values()) {
                if (eventId.getIdValue() == idValue) {
                    return eventId;
                }
            }
            return EventId.UNKNOWN;
        }

        public int getIdValue() {
            return idValue;
        }
    }

    public final EventId id;
    public final T value;
    public final long timestamp;

    public Event(EventId id, T value) {
        this(id, value, System.currentTimeMillis());
    }

    public Event(EventId id, T value, long timestamp) {
        this.id = id;
        this.value = value;
        this.timestamp = timestamp;
    }

    public EventId getId() {
        return id;
    }

    public T getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Event{")
                .append("id=").append(id)
                .append("value=").append(value)
                .append("timestamp=").append(timestamp)
                .append("}");
        return sb.toString();
    }
}
