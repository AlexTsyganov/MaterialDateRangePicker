package com.borax12.materialdaterangepicker.time;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by alex.tsyganov on 22/10/2017.
 */

@SuppressWarnings("WeakerAccess")
public interface TimepointLimiter extends Parcelable {
    boolean isOutOfRange(@Nullable TimepointCustom point, int index, @NonNull TimepointCustom.TYPE resolution);

    boolean isAmDisabled();

    boolean isPmDisabled();

    @NonNull
    TimepointCustom roundToNearest(
            @NonNull TimepointCustom time,
            @Nullable TimepointCustom.TYPE type,
            @NonNull TimepointCustom.TYPE resolution
    );
}