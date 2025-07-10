package com.example.wehab.operation;

import static com.example.wehab.protocal.Protocal.UUID_CHARACTERISTIC_WRITE;
import static com.example.wehab.protocal.Protocal.UUID_SERVICE;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;

import com.clj.fastble.exception.BleException;
import com.example.wehab.R;
import com.example.wehab.protocal.instruction.ImuConfig;
import com.example.wehab.protocal.instruction.InstSender;
import com.example.wehab.protocal.instruction.PpgConfig;


import java.util.Objects;

public class OperationActivity extends AppCompatActivity implements View.OnClickListener, OnDataDisplayFragmentDestroyListener{
    public static final String KEY_DATA = "key_data";
    private BleDevice bleDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);
        initView();
        initData();
    }

    @Override
    public void onClick(View v){
        int id = v.getId();
        InstSender instSender = new InstSender(bleDevice, UUID_SERVICE, UUID_CHARACTERISTIC_WRITE);
        BleWriteCallback bleWriteCallback = new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                Log.d("inst", "数据写入成功");
            }
            @Override
            public void onWriteFailure(BleException exception) {
                Log.d("inst", "数据写入失败 " + exception.toString());
            }
        };

        if (id == R.id.btn_start_imu) {
            instSender.sendInstruction("imuStart", bleWriteCallback);
            instSender.sendInstruction("ppgStop", bleWriteCallback);
        } else if (id == R.id.btn_start_ppg) {
            instSender.sendInstruction("ppgStart", bleWriteCallback);
            instSender.sendInstruction("imuStop", bleWriteCallback);
        }

        // 启动 DataDisplayFragment
        findViewById(R.id.main_ui_container).setVisibility(View.GONE);
        findViewById(R.id.data_display_container).setVisibility(View.VISIBLE);

        DataDisplayFragment fragment = DataDisplayFragment.newInstance(bleDevice);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.data_display_container, fragment)
                .addToBackStack(null)
                .commit();
    }
    @Override
    public void onResume(){
        super.onResume();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            FragmentManager fm = getSupportFragmentManager();
            Fragment currentFragment = fm.findFragmentById(R.id.data_display_container);

            if (currentFragment instanceof DataDisplayFragment) {
                Log.d("Back", "Back from fragment, popBackStack");
                fm.popBackStack(); // 返回上一层 Fragment
            } else {

                Log.d("Operation Activity finish", "back btn clicked in operation activity");
                finish(); // 回到 MainActivity
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentDestroy(){
        switchVisibility();
    }

    private void initView(){
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("获取数据");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Button btnStartImu = findViewById(R.id.btn_start_imu);
        btnStartImu.setOnClickListener(this);

        Button btnStartPpg = findViewById(R.id.btn_start_ppg);
        btnStartPpg.setOnClickListener(this);
    }

    private void initData(){
        // 获取对应的bleDevice
        bleDevice = getIntent().getParcelableExtra(KEY_DATA);
        if (bleDevice == null) {
            Log.d("Operation Activity Finish", "bleDevice is null");
            finish();}
    }

    private void switchVisibility(){
        findViewById(R.id.main_ui_container).setVisibility(View.VISIBLE);
        findViewById(R.id.data_display_container).setVisibility(View.GONE);
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
                            Log.d("instStop", "停止上传accel指令写成功");
                        }

                        @Override
                        public void onWriteFailure(BleException exception) {
                            Log.d("instStop", "停止上传accel指令写失败 " + exception.toString());
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