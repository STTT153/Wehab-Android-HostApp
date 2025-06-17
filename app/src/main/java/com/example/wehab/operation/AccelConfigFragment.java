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
import com.clj.fastble.utils.HexUtil;
import com.example.wehab.R;
import com.example.wehab.protocal.AccelConfig;

public class AccelConfigFragment extends DialogFragment {
    // UI相关变量
    private EditText editRange, editOrd, editInterval, editX, editY, editZ;
    private Button btnConfirm;
    // 业务逻辑相关变量
    public static final String KEY_DATA = "key_data";
    private BleDevice bleDevice;

    public AccelConfigFragment() {}

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
        editRange = view.findViewById(R.id.edit_range);
        editOrd = view.findViewById(R.id.edit_ord);
        editInterval = view.findViewById(R.id.edit_interval);
        editX = view.findViewById(R.id.edit_xoffset);
        editY = view.findViewById(R.id.edit_yoffset);
        editZ = view.findViewById(R.id.edit_zoffset);
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
                int range = Integer.parseInt(editRange.getText().toString().trim());
                int ord = Integer.parseInt(editOrd.getText().toString().trim());
                int interval = Integer.parseInt(editInterval.getText().toString().trim());
                int xOffset = Integer.parseInt(editX.getText().toString().trim());
                int yOffset = Integer.parseInt(editY.getText().toString().trim());
                int zOffset = Integer.parseInt(editZ.getText().toString().trim());

                AccelConfig config = new AccelConfig(range, ord, interval, xOffset, yOffset, zOffset,true);
                byte[] data = config.toHexByte();
                Log.d("inst", HexUtil.formatHexString(data, true));

                BleManager.getInstance().write(
                        bleDevice,
                        UUID_SERVICE,
                        UUID_CHARACTERISTIC_WRITE,
                        data,
                        new BleWriteCallback() {
                            @Override
                            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                android.widget.Toast.makeText(getContext(), "配置sensor指令发送数据到设备成功", android.widget.Toast.LENGTH_SHORT).show();
                                Log.d("inst", "发送accel指令到设备成功");
                            }

                            @Override
                            public void onWriteFailure(BleException exception) {
                                android.widget.Toast.makeText(getContext(), "配置sensor指令发送数据到设备失败", android.widget.Toast.LENGTH_SHORT).show();
                                Log.d("inst", "发送accel指令到设备成功");
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


