package com.dawning.frame.mvvm;

import androidx.arch.core.util.Function;

public abstract class FunctionSafety<I, O> implements Function<I, O> {
    @Override
    public O apply(I input) {
        O o = null;
        try {
            o = applySafety(input);
        } catch (Exception e) {

        }
        return o;
    }

    public abstract O applySafety(I input);

}