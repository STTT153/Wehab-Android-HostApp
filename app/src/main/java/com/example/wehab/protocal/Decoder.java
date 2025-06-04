package com.example.wehab.protocal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Decoder {
    public static String decode(byte[] data) {
        String rtn;
        if (data == null || data.length < 8) return "数据无效";

        byte type = data[3];

        switch (type) {
            case 0x01:
                return decodeAccel(data);
            case 0x02:
                return decodeGyro(data);
            case 3:
                rtn = "地磁传感器数据";
                break;
            case 4:
                rtn = "心率PPG";
                break;
            case 5:
                rtn = "血氧PPG";
                break;
            case 7:
                rtn = "温度传感器数据";
                break;
            case 8:
                rtn = "环境光感数据";
                break;
            case 9:
                if (data[10] == 1) rtn = "正在佩戴";
                else rtn = "未佩戴";
                break;
            case 10:
            case 42:
                rtn = "心率数据上传，备份数据上传";
                break;
            case 11:
            case 43:
                rtn = "当前血氧：" + data[10] + "%";
                break;
            case 12:
            case 50:
                rtn = "呼吸率测量结果";
                break;

            case 16:
                rtn = "计步器数据";
                break;
            case 17:
                rtn = "睡眠数据";
                break;
            case 19:
                rtn = "睡眠质量检测";
                break;
            case 21:
                if (data[10] == 2) rtn = "非活体佩戴";
                else rtn = "活体佩戴";
                break;
            case 22:
                rtn = "压力测量结果";
                break;
            case 32:
                rtn = "用户操作记录";
                break;
            case 33:
                rtn = "设备电量";
                break;
            default:
                rtn = "其他指令";
        }
        return rtn;
    }

    public static byte calcChecksum(byte[] data, int len) {
        int chkSum = 0;
        for (int i = 0; i < len; i++) {
            chkSum += data[i] & 0xFF;
        }
        return (byte) (~chkSum);
    }

    private static String decodeAccel(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        long seconds = buffer.getInt(4) & 0xFFFFFFFFL;
        int millis = buffer.getShort(8) & 0xFFFF;
        int imuStatus = buffer.get(10) & 0xFF;
        long timestampMs = seconds * 1000L + millis;

        StringBuilder result = new StringBuilder();
        result.append("加速度传感器数据\n");
        result.append("时间戳(ms): ").append(timestampMs).append("\n");
        result.append("IMU状态: ").append(imuStatus == 1 ? "50Hz" : "12.5Hz").append("\n");

        float range = 2.0f; // G 量程，可按需修改
        int offset = 11;
        while (offset + 6 <= data.length - 1) {
            short rawX = (short) ((data[offset] & 0xFF) | (data[offset + 1] << 8));
            short rawY = (short) ((data[offset + 2] & 0xFF) | (data[offset + 3] << 8));
            short rawZ = (short) ((data[offset + 4] & 0xFF) | (data[offset + 5] << 8));
            float x = rawX * range * 9.8f / 32768;
            float y = rawY * range * 9.8f / 32768;
            float z = rawZ * range * 9.8f / 32768;
            result.append(String.format("Accel[x=%.3f, y=%.3f, z=%.3f] g\n", x, y, z));
            offset += 12;
        }

        return result.toString();
    }

    private static String decodeGyro(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        long seconds = buffer.getInt(4) & 0xFFFFFFFFL;
        int millis = buffer.getShort(8) & 0xFFFF;
        int imuStatus = buffer.get(10) & 0xFF;
        long timestampMs = seconds * 1000L + millis;

        StringBuilder result = new StringBuilder();
        result.append("陀螺仪传感器数据\n");
        result.append("时间戳(ms): ").append(timestampMs).append("\n");
        result.append("IMU状态: ").append(imuStatus == 1 ? "50Hz" : "12.5Hz").append("\n");

        double dpsFactor = Math.pow(2, 7); // 假设为2000 dps
        double scale = (15.625 * dpsFactor * Math.PI / 180.0) / 32768.0;

        int offset = 11;
        while (offset + 6 <= data.length - 1) {
            short rawX = (short) ((data[offset] & 0xFF) | (data[offset + 1] << 8));
            short rawY = (short) ((data[offset + 2] & 0xFF) | (data[offset + 3] << 8));
            short rawZ = (short) ((data[offset + 4] & 0xFF) | (data[offset + 5] << 8));
            float x = (float) (rawX * scale);
            float y = (float) (rawY * scale);
            float z = (float) (rawZ * scale);
            result.append(String.format("Gyro[x=%.3f, y=%.3f, z=%.3f] deg/s\n", x, y, z));
            offset += 12;
        }

        return result.toString();
    }
}
