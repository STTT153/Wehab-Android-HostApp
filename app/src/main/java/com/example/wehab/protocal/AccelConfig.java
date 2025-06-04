package com.example.wehab.protocal;

public class AccelConfig {
    // 这个类中的所有数据均为10进制 integer
    private int rangeG; // 2, 4, 8, 16
    private int odrHz;  // 25, 50, 100, 200
    private int sendIntervalMs;
    private int offsetX;
    private int offsetY;
    private int offsetZ;
    private boolean enabled;

    public AccelConfig(int rangeG, int odrHz, int sendIntervalMs,
                       int offsetX, int offsetY, int offsetZ, boolean enabled) {
        this.rangeG = rangeG;
        this.odrHz = odrHz;
        this.sendIntervalMs = sendIntervalMs;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.enabled = enabled;
    }

    public byte[] toHexByte() {
        byte[] data = new byte[18]; // 17字节内容 + 1字节校验和

        // 1. Sync 2. Len
        data[0] = (byte) 0x5A;
        data[1] = (byte) 0xA5;
        data[2] = 0x0F;

        // 3. Type
        data[3] = (byte) 0x90;

        // 4. Range (2 bytes)
        data[4] = (byte) (rangeG & 0xFF);
        data[5] = (byte) ((rangeG >> 8) & 0xFF);

        // 5. ODR (2 bytes)
        data[6] = (byte) (odrHz & 0xFF);
        data[7] = (byte) ((odrHz >> 8) & 0xFF);

        // 6. Send Interval (2 bytes)
        data[8] = (byte) (sendIntervalMs & 0xFF);
        data[9] = (byte) ((sendIntervalMs >> 8) & 0xFF);

        // 7. Offset X (2 bytes)
        data[10] = (byte) (offsetX & 0xFF);
        data[11] = (byte) ((offsetX >> 8) & 0xFF);

        // 8. Offset Y (2 bytes)
        data[12] = (byte) (offsetY & 0xFF);
        data[13] = (byte) ((offsetY >> 8) & 0xFF);

        // 9. Offset Z (2 bytes)
        data[14] = (byte) (offsetZ & 0xFF);
        data[15] = (byte) ((offsetZ >> 8) & 0xFF);

        // 10. Enable
        data[16] = (byte) (enabled ? 1 : 0);

        // 11. Checksum: from data[0] to data[16] inclusive
        byte sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += data[i];
        }
        data[17] = (byte) (~sum); // 校验和

        return data;
    }




    // ---- Getters / Setters ----
    public int getRangeG() { return rangeG; }
    public void setRangeG(int rangeG) { this.rangeG = rangeG; }

    public int getOdrHz() { return odrHz; }
    public void setOdrHz(int odrHz) { this.odrHz = odrHz; }

    public int getSendIntervalMs() { return sendIntervalMs; }
    public void setSendIntervalMs(int sendIntervalMs) { this.sendIntervalMs = sendIntervalMs; }

    public int getOffsetX() { return offsetX; }
    public void setOffsetX(int offsetX) { this.offsetX = offsetX; }

    public int getOffsetY() { return offsetY; }
    public void setOffsetY(int offsetY) { this.offsetY = offsetY; }

    public int getOffsetZ() { return offsetZ; }
    public void setOffsetZ(int offsetZ) { this.offsetZ = offsetZ; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }


}