package com.example.wehab.operation;
import static com.example.wehab.protocal.Protocal.UUID_CHARACTERISTIC_NOTIFY;
import static com.example.wehab.protocal.Protocal.UUID_CHARACTERISTIC_WRITE;
import static com.example.wehab.protocal.Protocal.UUID_SERVICE;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;
import android.os.Bundle;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import com.clj.fastble.utils.HexUtil;
import com.example.wehab.R;
import com.example.wehab.protocal.AccelConfig;
import com.example.wehab.protocal.PpgConfig;
import com.example.wehab.protocal.SensorType;
import com.example.wehab.protocal.decode.Decoder;
import com.example.wehab.protocal.decode.SensorData;
import com.example.wehab.util.DownloadData;

import java.util.Objects;

public class DataDisplayFragment extends Fragment {
    private static final String KEY_DATA = "key_data";
    private OnDataDisplayFragmentDestroyListener listener;
    private BleDevice bleDevice;
    private Button btnSaveData, btnStopNotify;
    private Toolbar toolbar;
    private TextView accelX, accelY, accelZ;
    private TextView gyroX, gyroY, gyroZ;
    private TextView ppgG, ppgIR, ppgR;
    private boolean isNotifying = false;
    private final StringBuilder txtBuilder = new StringBuilder();
   

    public DataDisplayFragment() {}

    public static DataDisplayFragment newInstance(BleDevice bleDevice) {
        DataDisplayFragment fragment = new DataDisplayFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_DATA, bleDevice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bleDevice = getArguments().getParcelable(KEY_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data_display, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setUpListeners();
        setUpToolbar();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnDataDisplayFragmentDestroyListener) {
            listener = (OnDataDisplayFragmentDestroyListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentDestroyedListener");
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        BleManager.getInstance().stopNotify(bleDevice, UUID_SERVICE, UUID_CHARACTERISTIC_NOTIFY);
        stopRequiringData(); // 建议加入这一行，确保传感器停止上传
        listener.onFragmentDestroy();
    }

    private void updateSensorDisplay(byte[] data){
        SensorData parsedData = Decoder.decode(data);
        if (parsedData != null){
            SensorType sensorType = parsedData.getSensorType();
           switch (sensorType){
               case GYRO:
                   gyroX.setText(String.format("%.4f",parsedData.getFirstData()[0]));
                   gyroY.setText(String.format("%.4f",parsedData.getFirstData()[1]));
                   gyroZ.setText(String.format("%.4f",parsedData.getFirstData()[2]));
                   break;
               case ACCEL:
                   accelX.setText(String.format("%.4f",parsedData.getFirstData()[0]));
                   accelY.setText(String.format("%.4f",parsedData.getFirstData()[1]));
                   accelZ.setText(String.format("%.4f",parsedData.getFirstData()[2]));
                   break;
               case HEART_RATE_PPG:
                   ppgG.setText(String.format("%.4f",parsedData.getFirstData()[0]));
                   break;
               case BLOOD_OXYGEN_PPG:
                   ppgIR.setText(String.format("%.4f",parsedData.getFirstData()[0]));
                   ppgR.setText(String.format("%.4f",parsedData.getFirstData()[1]));
                   break;
           }
        }
    }

    private void runOnUiThread(Runnable runnable) {
        if (isAdded() && getActivity() != null)
            getActivity().runOnUiThread(runnable);
    }

    private void initView(View view){
        accelX = view.findViewById(R.id.accel_x);
        accelY = view.findViewById(R.id.accel_y);
        accelZ = view.findViewById(R.id.accel_z);

        gyroX = view.findViewById(R.id.gyro_x);
        gyroY = view.findViewById(R.id.gyro_y);
        gyroZ = view.findViewById(R.id.gyro_z);

        ppgG = view.findViewById(R.id.ppg_g);
        ppgIR = view.findViewById(R.id.ppg_ir);
        ppgR = view.findViewById(R.id.ppg_r);

        btnSaveData = view.findViewById(R.id.btn_save_data);
        btnStopNotify = view.findViewById(R.id.btn_swtich_notify_state);
        toolbar = view.findViewById(R.id.toolbar3);
    }

    private void setUpListeners(){
        btnSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
        btnStopNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNotifying) {
                    BleManager.getInstance().stopNotify(bleDevice, UUID_SERVICE, UUID_CHARACTERISTIC_NOTIFY);
                    btnStopNotify.setText("开始接收");
                }else{
                    startNotify();
                    btnStopNotify.setText("停止接收");
                }
                isNotifying = !isNotifying;
            }
        });
    }

    private void saveData(){
        DownloadData.saveStringToCSV("downloaded data", txtBuilder.toString(), getContext());
        txtBuilder.setLength(0);
    }

    private void setUpToolbar() {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);
        Objects.requireNonNull(activity.getSupportActionBar()).setTitle("数据看板");
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void startNotify(){
        BleManager.getInstance().notify(
                bleDevice,
                UUID_SERVICE,
                UUID_CHARACTERISTIC_NOTIFY,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        Log.d("notifyStatus","通知打开成功");
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        Log.d("notifyStatus","通知打开失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        SensorData sensorData = Decoder.decode(data);
                        if (sensorData != null) {
                            txtBuilder.append(sensorData.toString());
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateSensorDisplay(data);
                            }
                        });
                    }
                });
    }
    private void stopRequiringData(){
        AccelConfig accelConfig = new AccelConfig(16, 200, 100, 0, 0, 0,false);
        byte[] inst1 = accelConfig.toHexByte();
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
}