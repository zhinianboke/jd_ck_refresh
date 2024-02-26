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

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "CC";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private boolean serviceFlag = false;

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
        Log.i(TAG, "2");
        TextView v1 = findViewById(R.id.editText1);
        TextView v2 = findViewById(R.id.editText2);
        v1.setText(phoneNum1);
        v2.setText(phoneNum2);
        ToggleButton tb = findViewById(R.id.button);
        TextView messageVw = findViewById(R.id.myTextView);

        //在类里声明一个Handler


// 在Service中读取数据



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
                    editor.apply();
                    service.putExtra("phoneNum1",pn.getText().toString());
                    service.putExtra("phoneNum2",httpUrl.getText().toString());

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

}