package com.lafarge.truckmix.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.lafarge.truckmix.communicator.events.Event;

public class EventParcelable extends Event implements Parcelable     {

    public EventParcelable(Event event) {
        super(event.id, event.value, event.timestamp);
    }

    public EventParcelable(Parcel in) {
        super((Event.EventId) in.readValue(Event.EventId.class.getClassLoader()), in.readInt(), in.readLong());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(value.getClass().getClassLoader());
        dest.writeValue(id);
        dest.writeValue(value);
        dest.writeLong(timestamp);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new EventParcelable(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}
