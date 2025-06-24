package com.example.wehab.protocal;

public class PpgConfig {
    // 心率PPG配置字段
    private int mode;            // 测量模式：0~6
    private int interval;        // 定时测量时间间隔：5~60分钟
    private boolean enabled;     // 开关

    public PpgConfig(int mode, int interval, boolean enabled) {
        this.mode = mode;
        this.interval = interval;
        this.enabled = enabled;
    }

    public boolean isIntervalValid(){
        return this.interval >= 5 && this.interval <= 60;
    }

    public byte[] toHexByte() {
        byte[] data = new byte[7]; // 6字节内容 + 1字节校验和

        // 1. Sync (2 bytes)
        data[0] = (byte) 0x5A;
        data[1] = (byte) 0xA5;

        // 2. Len (1 byte)
        data[2] = 0x05;

        // 3. Type (1 byte)
        data[3] = (byte) 0x98;

        // 4. 测量模式 (1 byte)
        data[4] = (byte) (mode & 0xFF);

        // 5. 定时测量时间间隔 (1 byte)
        data[5] = (byte) (interval & 0xFF);

        // 6. 开关 (1 byte)
        data[6] = (byte) (enabled ? 1 : 0);

        // 7. 校验和: 从data[0]到data[6]的总和按位取反
        byte sum = 0;
        for (int i = 0; i <= 6; i++) {
            sum += data[i];
        }

        byte checksum = (byte) (~sum);

        // 添加校验和（返回新数组，含7 + 1 = 8字节）
        byte[] result = new byte[8];
        System.arraycopy(data, 0, result, 0, 7);
        result[7] = checksum;

        return result;
    }
}
