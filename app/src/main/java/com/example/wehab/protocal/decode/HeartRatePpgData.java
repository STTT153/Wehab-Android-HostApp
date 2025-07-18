package com.example.wehab.protocal.decode;

import androidx.annotation.NonNull;

import com.example.wehab.protocal.SensorType;

public class HeartRatePpgData extends SensorData {
    public float[] data;

    public HeartRatePpgData(SensorType sensorType, long timestamp, int groupCount){
        super(sensorType, timestamp, groupCount);
        data = new float[groupCount];
    }

    @Override
    public void setData(float... values){
        if (values.length != 1) throw new IllegalArgumentException("PpgData requires 1 float value");
        if (hasNext()) {
            data[index] = values[0];
            incrementIndex();
        } else {
            throw new IndexOutOfBoundsException("PpgData index out of range");
        }
    }

    @Override
    public float[] getFirstData(){
        return new float[]{data[0]};
    }

    @NonNull
    @Override
    public String toString(){
        StringBuilder result = new StringBuilder();
        result.append("G_PPG传感器\n");
        result.append("时间戳：").append(this.timestamp).append("\n");
        result.append("数据组数：").append(this.groupCount).append("\n");
        for (int i = 0; i < groupCount; i++){
            result.append("G_PPG: ").append(data[i]).append("\n");
        }
        return result.toString();
    }
}


