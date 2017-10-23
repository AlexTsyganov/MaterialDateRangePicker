package com.borax12.materialdaterangepicker.time;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by alex.tsyganov on 22/10/2017.
 */

class DefaultTimepointLimiter implements TimepointLimiter {
    private TreeSet<TimepointCustom> mSelectableTimes = new TreeSet<>();
    private TreeSet<TimepointCustom> mDisabledTimes = new TreeSet<>();
    private TreeSet<TimepointCustom> exclusiveSelectableTimes = new TreeSet<>();
    private TimepointCustom mMinTime;
    private TimepointCustom mMaxTime;

    DefaultTimepointLimiter() {
        final int M = selectable().size();
        setSelectableTimes(selectable().toArray(new TimepointCustom[M]));
    }

    private static final List<TimepointCustom> selectable() {
        List<TimepointCustom> result = new ArrayList<>();
        for (int y = 1; y <= 24; y++) {
            for (int i = 0; i < 4; i++) {
                result.add(new TimepointCustom(y,15*i,0));
            }
        }

        return result;
    }

    @SuppressWarnings("WeakerAccess")
    public DefaultTimepointLimiter(Parcel in) {
        mMinTime = in.readParcelable(TimepointCustom.class.getClassLoader());
        mMaxTime = in.readParcelable(TimepointCustom.class.getClassLoader());
        mSelectableTimes.addAll(Arrays.asList((TimepointCustom[]) in.readParcelableArray(TimepointCustom[].class.getClassLoader())));
        mDisabledTimes.addAll(Arrays.asList((TimepointCustom[]) in.readParcelableArray(TimepointCustom[].class.getClassLoader())));
        exclusiveSelectableTimes = getExclusiveSelectableTimes(mSelectableTimes, mDisabledTimes);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(mMinTime, flags);
        out.writeParcelable(mMaxTime, flags);
        out.writeParcelableArray((TimepointCustom[]) mSelectableTimes.toArray(), flags);
        out.writeParcelableArray((TimepointCustom[]) mDisabledTimes.toArray(), flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<DefaultTimepointLimiter> CREATOR
            = new Parcelable.Creator<DefaultTimepointLimiter>() {
        public DefaultTimepointLimiter createFromParcel(Parcel in) {
            return new DefaultTimepointLimiter(in);
        }

        public DefaultTimepointLimiter[] newArray(int size) {
            return new DefaultTimepointLimiter[size];
        }
    };

    void setMinTime(@NonNull TimepointCustom minTime) {
        if(mMaxTime != null && minTime.compareTo(mMaxTime) > 0)
            throw new IllegalArgumentException("Minimum time must be smaller than the maximum time");
        mMinTime = minTime;
    }

    void setMaxTime(@NonNull TimepointCustom maxTime) {
        if(mMinTime != null && maxTime.compareTo(mMinTime) < 0)
            throw new IllegalArgumentException("Maximum time must be greater than the minimum time");
        mMaxTime = maxTime;
    }

    void setSelectableTimes(@NonNull TimepointCustom[] selectableTimes) {
        mSelectableTimes.addAll(Arrays.asList(selectableTimes));
        exclusiveSelectableTimes = getExclusiveSelectableTimes(mSelectableTimes, mDisabledTimes);
    }

    void setDisabledTimes(@NonNull TimepointCustom[] disabledTimes) {
        mDisabledTimes.addAll(Arrays.asList(disabledTimes));
        exclusiveSelectableTimes = getExclusiveSelectableTimes(mSelectableTimes, mDisabledTimes);
    }

    private TreeSet<TimepointCustom> getExclusiveSelectableTimes(TreeSet<TimepointCustom> selectable, TreeSet<TimepointCustom> disabled) {
        TreeSet<TimepointCustom> output = (TreeSet<TimepointCustom>) selectable.clone();
        output.removeAll(disabled);
        return output;
    }

    @Override
    public boolean isOutOfRange(@Nullable TimepointCustom current, int index, @NonNull TimepointCustom.TYPE resolution) {
        if (current == null) return false;

        if (index == TimePickerDialog.HOUR_INDEX) {
            if (mMinTime != null && mMinTime.getHour() > current.getHour()) return true;

            if (mMaxTime != null && mMaxTime.getHour()+1 <= current.getHour()) return true;

            if (!exclusiveSelectableTimes.isEmpty()) {
                TimepointCustom ceil = exclusiveSelectableTimes.ceiling(current);
                TimepointCustom floor = exclusiveSelectableTimes.floor(current);
                return !(current.equals(ceil, TimepointCustom.TYPE.HOUR) || current.equals(floor, TimepointCustom.TYPE.HOUR));
            }

            if (!mDisabledTimes.isEmpty() && resolution == TimepointCustom.TYPE.HOUR) {
                TimepointCustom ceil = mDisabledTimes.ceiling(current);
                TimepointCustom floor = mDisabledTimes.floor(current);
                return current.equals(ceil, TimepointCustom.TYPE.HOUR) || current.equals(floor, TimepointCustom.TYPE.HOUR);
            }

            return false;
        }
        else if (index == TimePickerDialog.MINUTE_INDEX) {
            if (mMinTime != null) {
                TimepointCustom roundedMin = new TimepointCustom(mMinTime.getHour(), mMinTime.getMinute());
                if (roundedMin.compareTo(current) > 0) return true;
            }

            if (mMaxTime != null) {
                TimepointCustom roundedMax = new TimepointCustom(mMaxTime.getHour(), mMaxTime.getMinute(), 59);
                if (roundedMax.compareTo(current) < 0) return true;
            }

            if (!exclusiveSelectableTimes.isEmpty()) {
                TimepointCustom ceil = exclusiveSelectableTimes.ceiling(current);
                TimepointCustom floor = exclusiveSelectableTimes.floor(current);
                return !(current.equals(ceil, TimepointCustom.TYPE.MINUTE) || current.equals(floor, TimepointCustom.TYPE.MINUTE));
            }

            if (!mDisabledTimes.isEmpty() && resolution == TimepointCustom.TYPE.MINUTE) {
                TimepointCustom ceil = mDisabledTimes.ceiling(current);
                TimepointCustom floor = mDisabledTimes.floor(current);
                boolean ceilExclude = current.equals(ceil, TimepointCustom.TYPE.MINUTE);
                boolean floorExclude = current.equals(floor, TimepointCustom.TYPE.MINUTE);
                return ceilExclude || floorExclude;
            }

            return false;
        }
        else return isOutOfRange(current);
    }

    public boolean isOutOfRange(@NonNull TimepointCustom current) {
        if (mMinTime != null && mMinTime.compareTo(current) > 0) return true;

        if (mMaxTime != null && mMaxTime.compareTo(current) < 0) return true;

        if (!exclusiveSelectableTimes.isEmpty()) return !exclusiveSelectableTimes.contains(current);

        return mDisabledTimes.contains(current);
    }

    @Override
    public boolean isAmDisabled() {
        TimepointCustom midday = new TimepointCustom(12);

        if (mMinTime != null && mMinTime.compareTo(midday) >= 0) return true;

        if (!exclusiveSelectableTimes.isEmpty()) return exclusiveSelectableTimes.first().compareTo(midday) >= 0;

        return false;
    }

    @Override
    public boolean isPmDisabled() {
        TimepointCustom midday = new TimepointCustom(12);

        if (mMaxTime != null && mMaxTime.compareTo(midday) < 0) return true;

        if (!exclusiveSelectableTimes.isEmpty()) return exclusiveSelectableTimes.last().compareTo(midday) < 0;

        return false;
    }

    @Override
    public @NonNull
    TimepointCustom roundToNearest(@NonNull TimepointCustom time, @Nullable TimepointCustom.TYPE type, @NonNull TimepointCustom.TYPE resolution) {
        if (mMinTime != null && mMinTime.compareTo(time) > 0) return mMinTime;

        if (mMaxTime != null && mMaxTime.compareTo(time) < 0) return mMaxTime;

        // type == SECOND: cannot change anything, return input
        if (type == TimepointCustom.TYPE.SECOND) return time;

        if (!exclusiveSelectableTimes.isEmpty()) {
            TimepointCustom floor = exclusiveSelectableTimes.floor(time);
            TimepointCustom ceil = exclusiveSelectableTimes.ceiling(time);

            if (floor == null || ceil == null) {
                TimepointCustom t = floor == null ? ceil : floor;
                if (type == null) return t;
                if (t.getHour() != time.getHour()) return time;
                if (type == TimepointCustom.TYPE.MINUTE && t.getMinute() != time.getMinute()) return time;
                return t;
            }

            if (type == TimepointCustom.TYPE.HOUR) {
                if (floor.getHour() != time.getHour() && ceil.getHour() == time.getHour()) return ceil;
                if (floor.getHour() == time.getHour() && ceil.getHour() != time.getHour()) return floor;
                if (floor.getHour() != time.getHour() && ceil.getHour() != time.getHour()) return time;
            }

            if (type == TimepointCustom.TYPE.MINUTE) {
                if (floor.getHour() != time.getHour() && ceil.getHour() != time.getHour()) return time;
                if (floor.getHour() != time.getHour() && ceil.getHour() == time.getHour()) {
                    return ceil.getMinute() == time.getMinute() ? ceil : time;
                }
                if (floor.getHour() == time.getHour() && ceil.getHour() != time.getHour()) {
                    return floor.getMinute() == time.getMinute() ? floor : time;
                }
                if (floor.getMinute() != time.getMinute() && ceil.getMinute() == time.getMinute()) return ceil;
                if (floor.getMinute() == time.getMinute() && ceil.getMinute() != time.getMinute()) return floor;
                if (floor.getMinute() != time.getMinute() && ceil.getMinute() != time.getMinute()) return time;
            }

            int floorDist = Math.abs(time.compareTo(floor));
            int ceilDist = Math.abs(time.compareTo(ceil));

            return floorDist < ceilDist ? floor : ceil;
        }

        if (!mDisabledTimes.isEmpty()) {
            // if type matches resolution: cannot change anything, return input
            if (type != null && type == resolution) return time;

            if (resolution == TimepointCustom.TYPE.SECOND) {
                if (!mDisabledTimes.contains(time)) return time;
                return searchValidTimePoint(time, type, resolution);
            }

            if (resolution == TimepointCustom.TYPE.MINUTE) {
                TimepointCustom ceil = mDisabledTimes.ceiling(time);
                TimepointCustom floor = mDisabledTimes.floor(time);
                boolean ceilDisabled = time.equals(ceil, TimepointCustom.TYPE.MINUTE);
                boolean floorDisabled = time.equals(floor, TimepointCustom.TYPE.MINUTE);

                if (ceilDisabled || floorDisabled) return searchValidTimePoint(time, type, resolution);
                return time;
            }

            if (resolution == TimepointCustom.TYPE.HOUR) {
                TimepointCustom ceil = mDisabledTimes.ceiling(time);
                TimepointCustom floor = mDisabledTimes.floor(time);
                boolean ceilDisabled = time.equals(ceil, TimepointCustom.TYPE.HOUR);
                boolean floorDisabled = time.equals(floor, TimepointCustom.TYPE.HOUR);

                if (ceilDisabled || floorDisabled) return searchValidTimePoint(time, type, resolution);
                return time;
            }
        }

        return time;
    }

    private TimepointCustom searchValidTimePoint(@NonNull TimepointCustom time, @Nullable TimepointCustom.TYPE type, @NonNull TimepointCustom.TYPE resolution) {
        TimepointCustom forward = new TimepointCustom(time);
        TimepointCustom backward = new TimepointCustom(time);
        int iteration = 0;
        int resolutionMultiplier = 1;
        if (resolution == TimepointCustom.TYPE.MINUTE) resolutionMultiplier = 60;
        if (resolution == TimepointCustom.TYPE.SECOND) resolutionMultiplier = 3600;

        while (iteration < 24 * resolutionMultiplier) {
            iteration++;
            forward.add(resolution, 1);
            backward.add(resolution, -1);

            if (type == null || forward.get(type) == time.get(type)) {
                TimepointCustom forwardCeil = mDisabledTimes.ceiling(forward);
                TimepointCustom forwardFloor = mDisabledTimes.floor(forward);
                if (!forward.equals(forwardCeil, resolution) && !forward.equals(forwardFloor, resolution))
                    return forward;
            }

            if (type == null || backward.get(type) == time.get(type)) {
                TimepointCustom backwardCeil = mDisabledTimes.ceiling(backward);
                TimepointCustom backwardFloor = mDisabledTimes.floor(backward);
                if (!backward.equals(backwardCeil, resolution) && !backward.equals(backwardFloor, resolution))
                    return backward;
            }

            if (type != null && backward.get(type) != time.get(type) && forward.get(type) != time.get(type))
                break;
        }
        // If this step is reached, the user has disabled all timepoints
        return time;
    }
}
