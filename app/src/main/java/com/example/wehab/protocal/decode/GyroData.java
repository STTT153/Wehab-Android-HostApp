package com.example.wehab.protocal.decode;

import androidx.annotation.NonNull;

import com.example.wehab.protocal.SensorType;

public class GyroData extends SensorData {
    public float[] gyroX;
    public float[] gyroY;
    public float[] gyroZ;

    public GyroData(SensorType sensorType, long timestamp, int groupCount){
        super(sensorType, timestamp, groupCount);
        gyroX = new float[groupCount];
        gyroY = new float[groupCount];
        gyroZ = new float[groupCount];
    }

    @Override
    public void setData(float... values){
        if (values.length != 3) throw new IllegalArgumentException("AccelData requires 3 float values");
        if (hasNext()) {
            gyroX[index] = values[0];
            gyroY[index] = values[1];
            gyroZ[index] = values[2];
            incrementIndex();
        } else {
            throw new IndexOutOfBoundsException("AccelData index out of range");
        }
    }

    @Override
    public float[] getFirstData(){
        return new float[]{gyroX[0], gyroY[0], gyroZ[0]};
    }

    @NonNull
    @Override
    public String toString(){
        StringBuilder result = new StringBuilder();
        result.append("陀螺仪传感器\n");
        result.append("时间戳：").append(this.timestamp).append("\n");
        result.append("数据组数：").append(this.groupCount).append("\n");
        for (int i = 0; i < groupCount; i++){
            result.append("X: ").append(gyroX[i]).append("Y: ").append(gyroY[i]).append("Z: ").append(gyroZ[i]).append("\n");
        }
        return result.toString();
    }
}