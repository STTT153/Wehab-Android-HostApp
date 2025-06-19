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
import android.widget.ScrollView;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import com.clj.fastble.utils.HexUtil;
import com.example.wehab.R;
import com.example.wehab.protocal.AccelConfig;
import com.example.wehab.protocal.Decoder;
import com.example.wehab.protocal.PpgConfig;
import com.example.wehab.util.DownloadData;

import java.util.Objects;

public class DataDisplayFragment extends Fragment {
    private static final String KEY_DATA = "key_data";
    private OnDataDisplayFragmentDestroyListener listener;
    private BleDevice bleDevice;
    private Button btnSaveData;
    private Button btnStopNotify;
    private Toolbar toolbar;
    private TextView txt;
    private ScrollView scrollView;

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
        startNotify();
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
        listener.onFragmentDestroy();
    }

    private void addText(ScrollView scrollView, TextView textView, String content) {
        textView.append(content + "\n");
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void runOnUiThread(Runnable runnable) {
        if (isAdded() && getActivity() != null)
            getActivity().runOnUiThread(runnable);
    }

    private void initView(View view){
        txt = view.findViewById(R.id.tv_display);
        scrollView = view.findViewById(R.id.scroll_view);
        btnSaveData = view.findViewById(R.id.btn_save_data);
        btnStopNotify = view.findViewById(R.id.btn_stop_notify);
        toolbar = view.findViewById(R.id.toolbar3);
    }

    private void setUpListeners(){
        btnSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = txt.getText().toString(); // 获取 TextView 中的文本内容
                DownloadData.saveStringToCSV("downloaded data", content, getContext());
            }
        });
        btnStopNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRequiringData();
                BleManager.getInstance().stopNotify(bleDevice, UUID_SERVICE, UUID_CHARACTERISTIC_NOTIFY);
            }
        });
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addText(scrollView, txt, Decoder.decode(data));
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