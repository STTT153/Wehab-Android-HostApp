package com.example.wehab.protocal.decode;

import androidx.annotation.NonNull;

import com.example.wehab.protocal.SensorType;

public abstract class SensorData {
    protected SensorType sensorType;
    protected long timestamp;
    protected int groupCount;
    protected int size;
    protected int index;

    public SensorData(SensorType sensorType, long timestamp, int groupCount) {
        this.sensorType = sensorType;
        this.timestamp = timestamp;
        this.groupCount = groupCount;
        this.index = 0;
    }

    protected int getIndex() {
        return index;
    }

    protected boolean hasNext() {
        return index < groupCount;
    }

    protected void incrementIndex() {
        index++;
    }

    public SensorType getSensorType(){
        return this.sensorType;
    }

    public abstract void setData(float... values);

    public abstract float[] getFirstData();

    @NonNull
    public abstract String toString();
}
