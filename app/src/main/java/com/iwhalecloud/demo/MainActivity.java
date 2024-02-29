package com.iwhalecloud.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.iwhalecloud.demo.SMS.MyService;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity  implements  AdapterView.OnItemSelectedListener {


    private static final String TAG = "CC";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private boolean serviceFlag = false;

    private Spinner sp_dropdown;

    private String serverUrl;

    //定义下拉列表需要显示的文本数组
    private final static String[] starArray = {"ark.leafxxx.win", "login.ouklc.com", "jd.222798.xyz"};


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            TextView tv = (TextView) findViewById(R.id.myTextView);
            tv.setText(intent.getExtras().getString("message"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences read = getSharedPreferences("info", MODE_PRIVATE);
        String phoneNum1 = "";
        String phoneNum2= "";
        if (read.getString("phoneNum1",null)!=null){
            phoneNum1 = read.getString("phoneNum1",null);
        }
        if (read.getString("phoneNum2",null)!=null){
            phoneNum2 = read.getString("phoneNum2",null);
        }
        if (read.getString("serverUrl",null)!=null){
            serverUrl = read.getString("serverUrl",null);
        }
        TextView v1 = findViewById(R.id.editText1);
        TextView v2 = findViewById(R.id.editText2);
        v1.setText(phoneNum1);
        v2.setText(phoneNum2);
        ToggleButton tb = findViewById(R.id.button);
        sp_dropdown = findViewById(R.id.sp_dropdown);
        //声明一个下拉列表的数组适配器// 第一个参数：上下文，第二个参数：条目布局，第三个参数：要显示的数据
        ArrayAdapter<String> startAdapter = new ArrayAdapter<>(this, R.layout.item_select, starArray);

        //将适配器给Spinner
        sp_dropdown.setAdapter(startAdapter);

        //设置下拉框默认显示第一项
        int position = 0;
        for (int i = 0; i < starArray.length; i++) {
            if (starArray[i] == serverUrl) {
                position = i;
                break;
            }
        }


        sp_dropdown.setSelection(position);
//给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的onItemSelected方法
        sp_dropdown.setOnItemSelectedListener(this);




        IntentFilter filter = new IntentFilter("com.gdp2852.demo.service.broadcast");
        registerReceiver(receiver, filter);





        tb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView pn = findViewById(R.id.editText1);
                TextView httpUrl = findViewById(R.id.editText2);

                if (tb.isChecked()){
                    Intent service = new Intent(getApplicationContext(), MyService.class);
                    SharedPreferences.Editor editor = getSharedPreferences("info",MODE_PRIVATE).edit();
                    editor.putString("phoneNum1",pn.getText().toString());
                    editor.putString("phoneNum2",httpUrl.getText().toString());
                    editor.putString("serverUrl", serverUrl);
                    editor.apply();
                    service.putExtra("phoneNum1",pn.getText().toString());
                    service.putExtra("phoneNum2",httpUrl.getText().toString());
                    service.putExtra("serverUrl", serverUrl);

                    Log.i(TAG, "服务start"  + Build.VERSION.SDK_INT);
                    Log.i(TAG, "服务start"  + Build.VERSION_CODES.O);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        MainActivity.this.startService(service);
                    }
                    pn.setEnabled(false);
                    httpUrl.setEnabled(false);
                    Log.i(TAG, "服务开启");
                    Toast.makeText(MainActivity.this,"服务开启",Toast.LENGTH_SHORT).show();
                }else {
                    Intent service = new Intent(getApplicationContext(), MyService.class);
                    MainActivity.this.stopService(service);
                    pn.setEnabled(true);
                    httpUrl.setEnabled(true);
                    Toast.makeText(MainActivity.this,"服务停止",Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasReadSmsPermission = checkSelfPermission(Manifest.permission.READ_SMS);
            if (hasReadSmsPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent service = new Intent(getApplicationContext(), MyService.class);
        this.stopService(service);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        serverUrl = starArray[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}