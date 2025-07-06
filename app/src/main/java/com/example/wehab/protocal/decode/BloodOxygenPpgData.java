package com.example.wehab.protocal.decode;

import androidx.annotation.NonNull;

import com.example.wehab.protocal.SensorType;

public class BloodOxygenPpgData extends SensorData {
    public float[] irPpg;
    public float[] rPpg;

    public BloodOxygenPpgData(SensorType sensorType, long timestamp, int groupCount){
        super(sensorType, timestamp, groupCount);
        irPpg = new float[groupCount];
        rPpg = new float[groupCount];
    }

    @Override
    public void setData(float... values){
        // Data should be passed in irPpg rPpg manner.
        if (values.length != 2) throw new IllegalArgumentException("PpgData requires 1 float value");
        if (hasNext()) {
            irPpg[index] = values[0];
            rPpg[index] = values[1];
            incrementIndex();
        } else {
            throw new IndexOutOfBoundsException("PpgData index out of range");
        }
    }

    @Override
    public float[] getFirstData(){
        return new float[]{irPpg[0], rPpg[0]};
    }

    @NonNull
    @Override
    public String toString(){
        StringBuilder result = new StringBuilder();
        result.append("R_PPG传感器\n");
        result.append("时间戳：").append(this.timestamp).append("\n");
        result.append("数据组数：").append(this.groupCount).append("\n");
        for (int i = 0; i < groupCount; i++){
            result.append("R_PPG: ").append(rPpg[i]).append("\n");
        }
        result.append("IR_PPG传感器\n");
        result.append("时间戳：").append(this.timestamp).append("\n");
        result.append("数据组数：").append(this.groupCount).append("\n");
        for (int i = 0; i < groupCount; i++){
            result.append("IR_PPG: ").append(irPpg[i]).append("\n");
        }
        return result.toString();
    }
}


