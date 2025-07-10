package com.example.wehab.operation;

import static com.example.wehab.protocal.Protocal.UUID_CHARACTERISTIC_WRITE;
import static com.example.wehab.protocal.Protocal.UUID_SERVICE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.BleManager;

import com.clj.fastble.exception.BleException;
import com.example.wehab.R;
import com.example.wehab.protocal.instruction.ImuConfig;
import com.example.wehab.protocal.instruction.PpgConfig;

import android.widget.Spinner;
import android.widget.ArrayAdapter;

import java.util.concurrent.atomic.AtomicInteger;



public class SensorConfigFragment extends DialogFragment {
    // UI相关变量
    private Spinner spinnerRange, spinnerOdr, spinnerInterval, spinnerX, spinnerY, spinnerZ;
    private EditText editInterval;
    private Button btnConfirm;
    // 业务逻辑相关变量
    public static final String KEY_DATA = "key_data";
    private BleDevice bleDevice;

    public SensorConfigFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sensor_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
        setUpListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void initView(View view){
        spinnerRange = view.findViewById(R.id.spinner_range);
        spinnerOdr = view.findViewById(R.id.spinner_odr);
        spinnerInterval = view.findViewById(R.id.spinner_interval);
        spinnerX = view.findViewById(R.id.spinner_xoffset);
        spinnerY = view.findViewById(R.id.spinner_yoffset);
        spinnerZ = view.findViewById(R.id.spinner_zoffset);
        editInterval = view.findViewById(R.id.edit_interval);

        btnConfirm = view.findViewById(R.id.btn_confirm);

        setUpSpinner(spinnerRange, new String[]{"2", "4", "8", "16"});
        spinnerRange.setSelection(3);
        setUpSpinner(spinnerOdr, new String[]{"200", "100", "50", "25"});
        spinnerOdr.setSelection(0);
        setUpSpinner(spinnerInterval, new String[]{"50", "100", "200"});
        spinnerInterval.setSelection(0);

        setUpSpinner(spinnerX, new String[]{"0", "1", "2", "3"});
        setUpSpinner(spinnerY, new String[]{"0", "1", "2", "3"});
        setUpSpinner(spinnerZ, new String[]{"0", "1", "2", "3"});
    }

    private void initData(){
        if (getArguments() != null) {
            bleDevice = getArguments().getParcelable(KEY_DATA);
        }
    }

    private void setUpSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    private void setUpListeners(){
        btnConfirm.setOnClickListener(v -> {
            try {
                int range = Integer.parseInt(spinnerRange.getSelectedItem().toString());
                int ord = Integer.parseInt(spinnerOdr.getSelectedItem().toString());
                int interval = Integer.parseInt(spinnerInterval.getSelectedItem().toString());
                int xOffset = Integer.parseInt(spinnerX.getSelectedItem().toString());
                int yOffset = Integer.parseInt(spinnerY.getSelectedItem().toString());
                int zOffset = Integer.parseInt(spinnerZ.getSelectedItem().toString());
                int ppgInterval = Integer.parseInt(editInterval.getText().toString().trim());

                ImuConfig imuConfig = new ImuConfig(range, ord, interval, xOffset, yOffset, zOffset,true);
                byte[] inst1 = imuConfig.toHexByte();

                PpgConfig ppgConfig = new PpgConfig(2, ppgInterval, true);
                byte[] inst2 = ppgConfig.toHexByte();

                PpgConfig ppgConfig2 = new PpgConfig(5, ppgInterval, true);
                byte[] inst3 = ppgConfig2.toHexByte();

                sendAllConfigs(bleDevice, inst1, inst3, inst2);

            } catch (NumberFormatException e) {
                if (getContext() != null) {
                    android.widget.Toast.makeText(getContext(), "请输入有效数字", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void sendAllConfigs(BleDevice bleDevice, byte[] inst1, byte[] inst2, byte[] inst3) {
        AtomicInteger successCount = new AtomicInteger(0);
        int totalCommands = 3;

        BleWriteCallback callback = new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                int count = successCount.incrementAndGet();
                Log.d("instruction", "第 " + count + " 条指令发送成功");
                if (count == totalCommands) {
                    // 所有指令都成功后跳转页面
                    requireActivity().runOnUiThread(() -> {
                        requireActivity().findViewById(R.id.main_ui_container).setVisibility(View.GONE);
                        requireActivity().findViewById(R.id.data_display_container).setVisibility(View.VISIBLE);

                        DataDisplayFragment fragment = DataDisplayFragment.newInstance(bleDevice);
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.data_display_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    });
                }
            }

            @Override
            public void onWriteFailure(BleException exception) {
                android.widget.Toast.makeText(getContext(), "配置某个传感器指令失败: " + exception.getDescription(), android.widget.Toast.LENGTH_SHORT).show();
                Log.e("instruction", "指令发送失败: " + exception.toString());
                // 终止后续动作，无需再写入
            }
        };

        BleManager.getInstance().write(bleDevice, UUID_SERVICE, UUID_CHARACTERISTIC_WRITE, inst1, callback);
        BleManager.getInstance().write(bleDevice, UUID_SERVICE, UUID_CHARACTERISTIC_WRITE, inst2, callback);
        BleManager.getInstance().write(bleDevice, UUID_SERVICE, UUID_CHARACTERISTIC_WRITE, inst3, callback);
    }

}



