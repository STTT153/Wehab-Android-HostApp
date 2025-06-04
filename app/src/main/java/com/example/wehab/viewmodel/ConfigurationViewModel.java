package com.example.wehab.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.wehab.protocal.SensorType;
import com.example.wehab.protocal.AccelConfig;

public class ConfigurationViewModel extends ViewModel {
    private final MutableLiveData<SensorType> selectedSensor = new MutableLiveData<>();
    private final MutableLiveData<AccelConfig> accelConfig = new MutableLiveData<>();


    // getters
    public MutableLiveData<SensorType> getSelectedSensor() {
        return selectedSensor;
    }

    public MutableLiveData<AccelConfig> getAccelConfig() {
        return accelConfig;
    }

}


