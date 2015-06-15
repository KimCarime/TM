package com.lafarge.truckmix.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.lafarge.truckmix.communicator.events.Event;

public class EventParcelable<T> extends Event<T> implements Parcelable     {

    public EventParcelable(Event<T> event) {
        super(event.id, event.value, event.timestamp);
    }

    public EventParcelable(EventId id, T value, long timestamp) {
        super(id, value, timestamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(value.getClass().getName());
        dest.writeValue(id);
        dest.writeValue(value);
        dest.writeLong(timestamp);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            String type = in.readString();
            if (Integer.class.getName().equals(type)) {
                return new EventParcelable<Integer>(EventId.getEnum(in.readInt()), in.readInt(), in.readLong());
            } else if (Float.class.getName().equals(type)) {
                return new EventParcelable<Float>(EventId.getEnum(in.readInt()), in.readFloat(), in.readLong());
            } else {
                throw new IllegalArgumentException("unknown type " + type);
            }
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}
