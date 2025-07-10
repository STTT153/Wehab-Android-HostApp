package com.example.wehab.protocal.instruction;

import static com.example.wehab.protocal.Protocal.UUID_CHARACTERISTIC_WRITE;
import static com.example.wehab.protocal.Protocal.UUID_SERVICE;

import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import java.util.Arrays;
import java.util.List;

public class InstSender {
    final private BleDevice bleDevice;
    final private String uuidService;
    final private String uuidCharacteristic;

    public InstSender(BleDevice bleDevice, String uuidService, String uuidCharacteristic){
        this.bleDevice = bleDevice;
        this.uuidService = uuidService;
        this.uuidCharacteristic = uuidCharacteristic;
    }

    public void sendInstruction(String inst, BleWriteCallback bleWriteCallback)
                throws IllegalStateException, IllegalArgumentException{

        byte[] instruction;
        switch(inst){
            case "ppgStart":
                PpgConfig ppgConfig = new PpgConfig(3, 5, true);
                instruction = ppgConfig.toHexByte();
                break;
            case "imuStart":
                ImuConfig imuConfig = new ImuConfig(16, 200, 100, 0, 0, 0, true);
                instruction = imuConfig.toHexByte();
                break;
            case "ppgStop":
                PpgConfig ppgStop = new PpgConfig(3, 5, false);
                instruction = ppgStop.toHexByte();
                break;
            case "imuStop":
                ImuConfig imuStop = new ImuConfig(16, 200, 100, 0, 0, 0, false);
                instruction = imuStop.toHexByte();
                break;
            case "terminate":
                this.stopRequiringData();
            default:
                throw new IllegalArgumentException("Unknown argument given");
        }

        if (BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().write(this.bleDevice, this.uuidService, this.uuidCharacteristic, instruction, bleWriteCallback);
        }else{
            throw new IllegalStateException("BLE device is not connected.");
        }
    }

    private void stopRequiringData(){
        ImuConfig imuConfig = new ImuConfig(16, 200, 100, 0, 0, 0,false);
        byte[] inst1 = imuConfig.toHexByte();
        PpgConfig ppgConfig = new PpgConfig(2, 5, false);
        byte[] inst2 = ppgConfig.toHexByte();

        if (BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().write(bleDevice, UUID_SERVICE, UUID_CHARACTERISTIC_WRITE,
                    inst1,
                    new BleWriteCallback() {
                        @Override
                        public void onWriteSuccess(int current, int total, byte[] justWrite) {
                            Log.d("inst", "停止上传accel指令写成功");
                        }

                        @Override
                        public void onWriteFailure(BleException exception) {
                            Log.d("inst", "停止上传accel指令写失败 " + exception.toString());
                        }
                    });
            BleManager.getInstance().write(bleDevice, UUID_SERVICE, UUID_CHARACTERISTIC_WRITE,
                    inst2,
                    new BleWriteCallback() {
                        @Override
                        public void onWriteSuccess(int current, int total, byte[] justWrite) {
                            Log.d("inst", "停止上传ppg指令写成功");
                        }

                        @Override
                        public void onWriteFailure(BleException exception) {
                            Log.d("inst", "停止上传ppg指令写失败: " + exception.toString());
                        }
                    });
        } else {
            Log.e("BLE", "设备未连接");
        }
    }


    public void sendInstructionsSequentially(List<byte[]> instructions, BleDevice device,
                                             String uuidService, String uuidChar) {
        if (instructions == null || instructions.isEmpty()) return;

        sendNext(0, instructions, device, uuidService, uuidChar);
    }

    private void sendNext(int index, List<byte[]> instructions, BleDevice device,
                          String uuidService, String uuidChar) {
        if (index >= instructions.size()) return;

        BleManager.getInstance().write(
                device,
                uuidService,
                uuidChar,
                instructions.get(index),
                true, true, 0,
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        if (current == total) {
                            sendNext(index + 1, instructions, device, uuidService, uuidChar);
                        }
                    }
                    @Override
                    public void onWriteFailure(BleException exception) {
                        // 你也可以决定是否继续尝试下一条
                    }
                });
    }
}
