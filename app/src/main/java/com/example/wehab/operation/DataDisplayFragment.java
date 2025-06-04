package com.example.wehab.operation;
import static com.example.wehab.protocal.Protocal.uuid_characteristic_notify;
import static com.example.wehab.protocal.Protocal.uuid_service;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;

import com.example.wehab.MainActivity;
import com.example.wehab.R;
import com.example.wehab.protocal.Decoder;
import com.example.wehab.util.DownloadData;

public class DataDisplayFragment extends Fragment {
    private static final String KEY_DATA = "key_data";
    private BleDevice bleDevice;
    private Button btnSaveData;
    private Button btnStopNotify;

    private TextView tv_display;
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
        // 切换显示的容器
        requireActivity().findViewById(R.id.main_ui_container).setVisibility(View.GONE);
        requireActivity().findViewById(R.id.data_display_container).setVisibility(View.VISIBLE);

        View view = inflater.inflate(R.layout.fragment_data_display, container, false);

        tv_display = view.findViewById(R.id.tv_display);
        scrollView = view.findViewById(R.id.scroll_view);
        btnSaveData = view.findViewById(R.id.btn_save_data);
        btnStopNotify = view.findViewById(R.id.btn_stop_notify);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        BleManager.getInstance().notify(
                bleDevice,
                uuid_service,
                uuid_characteristic_notify,
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
                                addText(scrollView, tv_display, Decoder.decode(data));
                            }
                        });
                    }
                });

        btnSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = tv_display.getText().toString(); // 获取 TextView 中的文本内容
                DownloadData.saveStringToCSV("downloaded data", content, getContext());
            }
        });

        btnStopNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BleManager.getInstance().stopNotify(bleDevice, uuid_service, uuid_characteristic_notify);
            }
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().findViewById(R.id.main_ui_container).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.data_display_container).setVisibility(View.GONE);
        BleManager.getInstance().stopNotify(bleDevice, uuid_service, uuid_characteristic_notify);
    }

    private void addText(ScrollView scrollView, TextView textView, String content) {
        textView.append(content + "\n");
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void runOnUiThread(Runnable runnable) {
        if (isAdded() && getActivity() != null)
            getActivity().runOnUiThread(runnable);
    }


}