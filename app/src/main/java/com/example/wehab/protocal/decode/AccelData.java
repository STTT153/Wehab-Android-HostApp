package com.example.wehab.protocal.decode;

import androidx.annotation.NonNull;

import com.example.wehab.protocal.SensorType;

public class AccelData extends SensorData {
    public float[] accelX;
    public float[] accelY;
    public float[] accelZ;

    public AccelData(SensorType sensorType, long timestamp, int groupCount){
        super(sensorType, timestamp, groupCount);
        accelX = new float[groupCount];
        accelY = new float[groupCount];
        accelZ = new float[groupCount];
    }

    @Override
    public void setData(float... values){
        if (values.length != 3) throw new IllegalArgumentException("AccelData requires 3 float values");
        if (hasNext()) {
            accelX[index] = values[0];
            accelY[index] = values[1];
            accelZ[index] = values[2];
            incrementIndex();
        } else {
            throw new IndexOutOfBoundsException("AccelData index out of range");
        }
    }

    @Override
    public float[] getFirstData(){
        return new float[]{accelX[0], accelY[0], accelZ[0]};
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("加速度传感器\n");
        result.append("时间戳：").append(this.timestamp).append("\n");
        result.append("数据组数：").append(this.groupCount).append("\n");
        for (int i = 0; i < groupCount; i++) {
            result.append("X: ").append(accelX[i]).append(", Y: ").append(accelY[i]).append(", Z: ").append(accelZ[i]).append("\n");
        }
        return result.toString();
    }
}


