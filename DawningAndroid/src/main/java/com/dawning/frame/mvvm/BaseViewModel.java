package com.dawning.frame.mvvm;

import androidx.arch.core.util.Function;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public abstract class BaseViewModel<T> extends ViewModel {

    protected MutableLiveData<T> liveData;

    public MutableLiveData<T> getData() {
        if (liveData == null) {
            liveData = new MutableLiveData<>();
            loadData();
        }
        return liveData;
    }

    protected void postValue(T t) {
        if (liveData != null) {
            liveData.postValue(t);
        }
    }

    protected abstract void loadData();

    public  <K> MediatorLiveData<K> transformations(final Function<T, K> function) {
        return (MediatorLiveData<K>) Transformations.map(liveData, new Function<T, K>() {
            @Override
            public K apply(T input) {
                return function.apply(input);
            }
        });
    }

}
