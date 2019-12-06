package com.dawning.frame.mvvm;

import androidx.lifecycle.Observer;
import androidx.annotation.Nullable;

public abstract class ObserverSafety<T> implements Observer<T> {
    @Override
    public void onChanged(@Nullable T t) {
        try {
            onChangedSafety(t);
        } catch (Exception e) {

        }
    }

    public abstract void onChangedSafety(@Nullable T t);
}
