package com.borax12.materialdaterangepicker.time;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by alex.tsyganov on 22/10/2017.
 */

@SuppressWarnings("WeakerAccess")
public interface TimepointLimiter extends Parcelable {
    boolean isOutOfRange(@Nullable Timepoint point, int index, @NonNull Timepoint.TYPE resolution);

    boolean isAmDisabled();

    boolean isPmDisabled();

    @NonNull Timepoint roundToNearest(
            @NonNull Timepoint time,
            @Nullable Timepoint.TYPE type,
            @NonNull Timepoint.TYPE resolution
    );
}