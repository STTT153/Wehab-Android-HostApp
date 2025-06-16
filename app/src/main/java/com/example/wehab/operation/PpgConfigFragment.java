package com.example.wehab.operation;

import static com.example.wehab.protocal.Protocal.UUID_CHARACTERISTIC_WRITE;
import static com.example.wehab.protocal.Protocal.UUID_SERVICE;

import android.os.Bundle;
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
import com.example.wehab.protocal.PpgConfig;

public class PpgConfigFragment extends DialogFragment {
    // UI相关变量
    private EditText editInterval;
    private Button btnConfirm;
    // 业务逻辑相关变量
    public static final String KEY_DATA = "key_data";
    private BleDevice bleDevice;

    public PpgConfigFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accel_config, container, false);
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
        editInterval = view.findViewById(R.id.edit_interval);
        btnConfirm = view.findViewById(R.id.btn_confirm);
    }

    private void initData(){
        if (getArguments() != null) {
            bleDevice = getArguments().getParcelable(KEY_DATA);
        }
    }

    private void setUpListeners(){
        btnConfirm.setOnClickListener(v -> {
            try {
                int interval = Integer.parseInt(editInterval.getText().toString().trim());
                PpgConfig config = new PpgConfig(3, interval, true);
                byte[] data = config.toHexByte();

                BleManager.getInstance().write(
                        bleDevice,
                        UUID_SERVICE,
                        UUID_CHARACTERISTIC_WRITE,
                        data,
                        new BleWriteCallback() {
                            @Override
                            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                android.widget.Toast.makeText(getContext(), "发送数据到设备成功", android.widget.Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onWriteFailure(BleException exception) {
                                android.widget.Toast.makeText(getContext(), "发送数据到设备失败", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });

                // 切换显示的容器
                requireActivity().findViewById(R.id.main_ui_container).setVisibility(View.GONE);
                requireActivity().findViewById(R.id.data_display_container).setVisibility(View.VISIBLE);

                // 添加数据看板页面
                DataDisplayFragment fragment = DataDisplayFragment.newInstance(bleDevice);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.data_display_container, fragment)
                        .addToBackStack(null)
                        .commit();

            } catch (NumberFormatException e) {
                if (getContext() != null) {
                    android.widget.Toast.makeText(getContext(), "请输入有效数字", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
