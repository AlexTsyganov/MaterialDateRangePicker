package com.borax12.materialdaterangepicker.time;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by alex.tsyganov on 22/10/2017.
 */

@SuppressWarnings("WeakerAccess")
public class TimepointCustom implements Parcelable, Comparable<TimepointCustom> {
    private int hour;
    private int minute;
    private int second;

    public enum TYPE {
        HOUR,
        MINUTE,
        SECOND
    }

    public TimepointCustom(TimepointCustom time) {
        this(time.hour, time.minute, time.second);
    }

    public TimepointCustom(@IntRange(from=0, to=23) int hour,
                           @IntRange(from=0, to=59) int minute,
                           @IntRange(from=0, to=59) int second) {
        this.hour = hour % 24;
        this.minute = minute % 60;
        this.second = second % 60;
    }

    public TimepointCustom(@IntRange(from=0, to=23) int hour,
                           @IntRange(from=0, to=59) int minute) {
        this(hour, minute, 0);
    }

    public TimepointCustom(@IntRange(from=0, to=23) int hour) {
        this(hour, 0);
    }

    public TimepointCustom(Parcel in) {
        hour = in.readInt();
        minute = in.readInt();
        second = in.readInt();
    }

    @IntRange(from=0, to=23)
    public int getHour() {
        return hour;
    }

    @IntRange(from=0, to=59)
    public int getMinute() {
        return minute;
    }

    @IntRange(from=0, to=59)
    public int getSecond() {
        return second;
    }

    public boolean isAM() {
        return hour < 12;
    }

    public boolean isPM() {
        return !isAM();
    }

    public void setAM() {
        if(hour >= 12) hour = hour % 12;
    }

    public void setPM() {
        if(hour < 12) hour = (hour + 12) % 24;
    }

    public void add(TYPE type, int value) {
        if (type == TYPE.MINUTE) value *= 60;
        if (type == TYPE.HOUR) value *= 3600;
        value += toSeconds();

        switch (type) {
            case SECOND:
                second = (value % 3600) % 60;
            case MINUTE:
                minute = (value % 3600) / 60;
            case HOUR:
                hour = (value / 3600) % 24;
        }
    }

    public int get(@NonNull TYPE type) {
        switch (type) {
            case SECOND:
                return getSecond();
            case MINUTE:
                return getMinute();
            case HOUR:
            default: // Makes the compiler happy
                return getHour();
        }
    }

    public int toSeconds() {
        return 3600 * hour + 60 * minute + second;
    }

    @Override
    public int hashCode() {
        return toSeconds();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimepointCustom timepoint = (TimepointCustom) o;

        return hashCode() == timepoint.hashCode();
    }

    public boolean equals(@Nullable TimepointCustom time, @NonNull TYPE resolution) {
        if (time == null) return false;
        boolean output = true;
        switch (resolution) {
            case SECOND:
                output = output && time.getSecond() == getSecond();
            case MINUTE:
                output = output && time.getMinute() == getMinute();
            case HOUR:
                output = output && time.getHour() == getHour();
        }
        return output;
    }

    @Override
    public int compareTo(@NonNull TimepointCustom t) {
        return hashCode() - t.hashCode();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(hour);
        out.writeInt(minute);
        out.writeInt(second);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<TimepointCustom> CREATOR
            = new Parcelable.Creator<TimepointCustom>() {
        public TimepointCustom createFromParcel(Parcel in) {
            return new TimepointCustom(in);
        }

        public TimepointCustom[] newArray(int size) {
            return new TimepointCustom[size];
        }
    };

    @Override
    public String toString() {
        return "" + hour + "h " + minute + "m " + second + "s";
    }
}