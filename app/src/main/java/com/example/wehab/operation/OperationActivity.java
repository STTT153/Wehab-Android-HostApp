package com.example.wehab.operation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.view.MenuItem;
import android.widget.Button;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.clj.fastble.data.BleDevice;

import com.example.wehab.operation.AccelConfigFragment;
import com.example.wehab.R;
import com.example.wehab.viewmodel.ConfigurationViewModel;


public class OperationActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String KEY_DATA = "key_data";
    private Button btn_ppg;
    private Button btn_acc;
    private Button btn_gyro;
    private BleDevice bleDevice;
    public  ConfigurationViewModel viewModel;

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
        /***
        if (id == R.id.btn_acc){
            intent.putExtra(DATA_TYPE, "acc_data");
        }else if (id == R.id.btn_gyro) {
            intent.putExtra(DATA_TYPE, "gyro_data");
        }else if (id == R.id.btn_ppg) {
            intent.putExtra(DATA_TYPE, "ppg_data");
        }***/

        // 启动 AccelConfigFragment 页面
        AccelConfigFragment fragment = new AccelConfigFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.config_container, fragment)
                .addToBackStack(null)
                .commit();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_DATA, bleDevice);
        fragment.setArguments(bundle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("获取数据");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 显示箭头
        getSupportActionBar().setHomeButtonEnabled(true);      // 启用点击事件

        btn_ppg = findViewById(R.id.btn_ppg);
        btn_ppg.setOnClickListener(this);
        btn_acc = findViewById(R.id.btn_acc);
        btn_acc.setOnClickListener(this);
        btn_gyro = findViewById(R.id.btn_gyro);
        btn_gyro.setOnClickListener(this);
    }

    private void initData(){
        // 获取对应的bleDevice
        bleDevice = getIntent().getParcelableExtra(KEY_DATA);
        if (bleDevice == null) {finish();}
    }
}