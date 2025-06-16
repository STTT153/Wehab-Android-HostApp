package com.example.wehab.protocal;

import android.annotation.SuppressLint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Decoder {
    public static String decode(byte[] data) {
        String rtn;
        if (data == null || data.length < 8) return "数据无效";

        byte type = data[3];

        switch (type) {
            case 0x01:
                // Log.d("Accel Raw Data", HexUtil.formatHexString(data, true));
                return decodeAccel(data);
            case 0x02:
                return decodeGyro(data);
            case 3:
                rtn = "地磁传感器数据";
                break;
            case 4:
                return decodePpg(data);
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

    private static boolean verifyChecksum(byte[] data) {
        int lengthToCheck = data.length - 1; // 最后一字节是SUM
        byte checksum = 0;

        for (int i = 0; i < lengthToCheck; i++) {
            checksum += data[i];
        }

        checksum = (byte) ~checksum;
        return (checksum == data[data.length - 1]);
    }


    @SuppressLint("DefaultLocale")
    private static String decodeAccel(byte[] data) {
        if (!verifyChecksum(data)) {
            return "校验和错误，数据无效";
        }

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        int length = buffer.get(2) & 0xFF;

        if (data.length != (length + 3)) { // +3 是 Sync(2B) + Len(1B)
            return "数据长度与长度字段不一致";
        }

        int type = buffer.get(3) & 0xFF;
        if (type != 0x01) {
            return "不是加速度数据类型";
        }

        long seconds = buffer.getInt(4) & 0xFFFFFFFFL;
        int millis = buffer.getShort(8) & 0xFFFF;
        int imuStatus = buffer.get(10) & 0xFF;
        long timestampMs = seconds * 1000L + millis;

        int offset = 11;
        int accelGroupCount = (length - 8) / 12;

        StringBuilder result = new StringBuilder();
        result.append("加速度传感器数据\n");
        result.append("时间戳(ms): ").append(timestampMs).append("\n");
        result.append("IMU状态: ").append(imuStatus == 1 ? "50Hz" : "12.5Hz").append("\n");
        result.append("加速度组数: ").append(accelGroupCount).append("\n");

        int i = 0;
        while (offset + 12 <= data.length - 1) { // 确保不读取到校验和字节
            float x = buffer.getFloat(offset);
            float y = buffer.getFloat(offset + 4);
            float z = buffer.getFloat(offset + 8);

            result.append(String.format("Accel[%d] x=%.3f, y=%.3f, z=%.3f g\n", i, x, y, z));
            offset += 12;
            i++;
        }

        result.append("实际解析组数: ").append(i).append("\n");
        result.append("结束偏移位置: ").append(offset).append(" / 总长度: ").append(data.length).append("\n");

        return result.toString();
    }

    private static String decodeGyro(byte[] data) {
        if (!verifyChecksum(data)) {
            return "校验和错误，数据无效";
        }

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        int length = buffer.get(2) & 0xFF;

        if (data.length != (length + 3)) { // +3 是 Sync(2B) + Len(1B)
            return "数据长度与长度字段不一致";
        }

        int type = buffer.get(3) & 0xFF;
        if (type != 0x02) {
            return "不是陀螺仪数据类型";
        }

        long seconds = buffer.getInt(4) & 0xFFFFFFFFL;
        int millis = buffer.getShort(8) & 0xFFFF;
        int imuStatus = buffer.get(10) & 0xFF;
        long timestampMs = seconds * 1000L + millis;

        int offset = 11;
        int gyroGroupCount = (length - 8) / 12;

        StringBuilder result = new StringBuilder();
        result.append("陀螺仪传感器数据\n");
        result.append("时间戳(ms): ").append(timestampMs).append("\n");
        result.append("IMU状态: ").append(imuStatus == 1 ? "50Hz" : "12.5Hz").append("\n");
        result.append("陀螺仪组数: ").append(gyroGroupCount).append("\n");

        int i = 0;

        while (offset + 12 <= data.length - 1) { // 不读到校验和字节
            float x = buffer.getFloat(offset);
            float y = buffer.getFloat(offset + 4);
            float z = buffer.getFloat(offset + 8);

            result.append(String.format("Gyro[%d] x=%.3f, y=%.3f, z=%.3f deg/s\n", i, x, y, z));
            offset += 12;
            i++;
        }

        result.append("实际解析组数: ").append(i).append("\n");
        result.append("结束偏移位置: ").append(offset).append(" / 总长度: ").append(data.length).append("\n");

        return result.toString();
    }


    private static String decodePpg(byte[] data) {
        if (!verifyChecksum(data)) {
            return "校验和错误，数据无效";
        }

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        int length = buffer.get(2) & 0xFF;
        if (data.length != (length + 3)) { // Sync(2B) + Len(1B)
            return "数据长度与长度字段不一致";
        }

        int type = buffer.get(3) & 0xFF;
        if (type != 0x04) {
            return "不是心率PPG数据类型";
        }

        long seconds = buffer.getInt(4) & 0xFFFFFFFFL;
        int millis = buffer.getShort(8) & 0xFFFF;
        long timestampMs = seconds * 1000L + millis;

        int offset = 10;
        int groupSize = 5; // 4 bytes data + 1 byte flag
        int groupCount = (length - 7) / groupSize; // 减去 type(1) + seconds(4) + millis(2)

        StringBuilder result = new StringBuilder();
        result.append("心率 PPG 数据\n");
        result.append("时间戳(ms): ").append(timestampMs).append("\n");
        result.append("数据组数: ").append(groupCount).append("\n");

        int i = 0;
        while (offset + groupSize <= data.length - 1) { // 留出校验和
            long value = buffer.getInt(offset) & 0xFFFFFFFFL;
            int flag = buffer.get(offset + 4) & 0xFF;

            result.append(String.format("PPG[%d] Value=%d, 调制=%s\n",
                    i, value, flag == 1 ? "是" : "否"));

            offset += groupSize;
            i++;
        }

        result.append("实际解析组数: ").append(i).append("\n");
        result.append("结束偏移位置: ").append(offset).append(" / 总长度: ").append(data.length).append("\n");

        return result.toString();
    }


}
