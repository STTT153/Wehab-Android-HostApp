package com.example.wehab.operation;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.view.MenuItem;
import android.widget.Button;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.clj.fastble.data.BleDevice;

import com.example.wehab.R;

import java.util.Objects;

public class OperationActivity extends AppCompatActivity implements View.OnClickListener{
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
        if (id == R.id.btn_acc){
            // 启动 AccelConfigFragment 页面
            AccelConfigFragment fragment = new AccelConfigFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.config_container, fragment)
                    .addToBackStack(null)
                    .commit();
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_DATA, bleDevice);
            fragment.setArguments(bundle);
        }else if (id == R.id.btn_ppg) {
            // 启动 PpgConfigFragment 页面
            PpgConfigFragment fragment = new PpgConfigFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.config_container, fragment)
                    .addToBackStack(null)
                    .commit();
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_DATA, bleDevice);
            fragment.setArguments(bundle);
        }
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

    private void initView(){
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("获取数据");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Button btnPpg = findViewById(R.id.btn_ppg);
        btnPpg.setOnClickListener(this);

        Button btnAcc = findViewById(R.id.btn_acc);
        btnAcc.setOnClickListener(this);
    }

    private void initData(){
        // 获取对应的bleDevice
        bleDevice = getIntent().getParcelableExtra(KEY_DATA);
        if (bleDevice == null) {
            Log.d("Operation Activity Finish", "bleDevice is null");
            finish();}
    }
}