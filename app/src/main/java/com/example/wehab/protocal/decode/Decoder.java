package com.example.wehab.protocal.decode;

import android.annotation.SuppressLint;
import android.util.Log;

import com.clj.fastble.utils.HexUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.wehab.protocal.SensorType;

public class Decoder {
    public static SensorData decode(byte[] data) {
        byte type = data[3];
        return switch (type) {
            case 0x01 -> decodeAccel(data);
            case 0x02 -> decodeGyro(data);
            case 0x04 -> decodePpg(data);
            default -> null;
        };
    }

    private static boolean isChecksumValid(byte[] data) {
        int lengthToCheck = data.length - 1; // 最后一字节是SUM
        byte checksum = 0;
        for (int i = 0; i < lengthToCheck; i++) {
            checksum += data[i];
        }
        checksum = (byte) ~checksum;
        return (checksum == data[data.length - 1]);
    }
    private static boolean isLengthValid(byte[] data){
        int len = data[2] & 0xFF;
        return len + 3 == data.length;
    }
    private static boolean isDataValid(byte[] data){
        return isChecksumValid(data) && isLengthValid(data);
    }

    @SuppressLint("DefaultLocale")
    private static SensorData decodeAccel(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        int length = buffer.get(2) & 0xFF;
        long seconds = buffer.getInt(4) & 0xFFFFFFFFL;
        int millis = buffer.getShort(8) & 0xFFFF;
        long timestampMs = seconds * 1000L + millis;
        int accelGroupCount = (length - 8) / 12;
        AccelData parsedData = new AccelData(SensorType.ACCEL, timestampMs, accelGroupCount);

        if (!isDataValid(data)) {
            return null;
        }

        int i = 0;
        int offset = 10;

        while (offset + 12 <= data.length - 1) { // 确保不读取到校验和字节
            float x = buffer.getFloat(offset);
            float y = buffer.getFloat(offset + 4);
            float z = buffer.getFloat(offset + 8);

            parsedData.setData(x, y, z);
            offset += 12;
            i++;
        }

        return parsedData;
    }

    @SuppressLint("DefaultLocale")
    private static SensorData decodeGyro(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        int length = buffer.get(2) & 0xFF;
        long seconds = buffer.getInt(4) & 0xFFFFFFFFL;
        int millis = buffer.getShort(8) & 0xFFFF;
        long timestampMs = seconds * 1000L + millis;
        int gyroGroupCount = (length - 8) / 12;

        GyroData parsedData = new GyroData(SensorType.GYRO, timestampMs, gyroGroupCount);

        if (!isDataValid(data)) {
            return null;
        }

        int offset = 10;

        while (offset + 12 <= data.length - 1) { // 不读到校验和字节
            float x = buffer.getFloat(offset);
            float y = buffer.getFloat(offset + 4);
            float z = buffer.getFloat(offset + 8);

            parsedData.setData(x, y, z);

            offset += 12;
        }

        return parsedData;
    }

    private static SensorData decodePpg(byte[] data) {
        if (!isDataValid(data)) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        int length = buffer.get(2) & 0xFF;
        long seconds = buffer.getInt(4) & 0xFFFFFFFFL;
        int millis = buffer.getShort(8) & 0xFFFF;
        long timestampMs = seconds * 1000L + millis;

        int offset = 10;
        int groupCount = (length - 7) / 4;

        PpgData parsedData = new PpgData(SensorType.PPG, timestampMs, groupCount);

        while (offset + 4 <= data.length - 1) { // 留出校验和
            long value = buffer.getInt(offset) & 0xFFFFFFFFL;
            parsedData.setData(value);

            offset += 4;
        }
        return parsedData;
    }
}
