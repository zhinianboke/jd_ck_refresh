package com.iwhalecloud.demo.SMS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.iwhalecloud.demo.MainActivity;
import com.iwhalecloud.demo.R;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyService extends Service {
    private static final String TAG = MyService.class.getSimpleName();
    private SMSContentObserver smsObserver;

    private String phoneNum1;

    private String phoneNum2;

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        /**
         * 获取当前Service的实例
         * @return
         */
        public MyService getService() {
            return MyService.this;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        //注册观察者
        smsObserver = new SMSContentObserver(MyService.this, new Handler());
        getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, smsObserver);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        phoneNum1 = intent.getStringExtra("phoneNum1");
        phoneNum2 = intent.getStringExtra("phoneNum2");
        startForeground(100, getNotification("服务运行中...", "正在监听号码:" + phoneNum1 + "," + phoneNum2));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(smsObserver);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 唯一的通知通道的id.
        String notificationChannelId = "notification_channel_id_01";
        // Android8.0以上的系统，新建消息通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //用户可见的通道名称
            String channelName = "Foreground Service Notification";
            //通道的重要程度
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, channelName, importance);
            notificationChannel.setDescription("Channel description");
            //LED灯
            notificationChannel.enableLights(false);
            //震动
            notificationChannel.enableVibration(false);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId);
        //通知小图标
        builder.setSmallIcon(R.mipmap.ic_launcher);
        //通知标题
        builder.setContentTitle(title);
        //通知内容
        builder.setContentText(message);
        //设定通知显示的时间
        builder.setWhen(System.currentTimeMillis());
        //设定启动的内容
        Intent clickIntent = new Intent(Intent.ACTION_MAIN);
        //点击回到活动主页 而不是创建新主页
        clickIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        clickIntent.setComponent(new ComponentName(this, MainActivity.class));
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        //创建通知并返回
        return builder.build();
    }

    @SuppressLint("Range")
    private void setSmsCode() {
        Toast.makeText(MyService.this, phoneNum1 + ":" + phoneNum2, Toast.LENGTH_SHORT).show();
        Cursor cursor = null;
        // 添加异常捕捉
        try {
            cursor = getContentResolver().query(
                    Uri.parse("content://sms/inbox"),
                    new String[]{"_id", "address", "body", "date"},
                    null, null, "_id desc");
            if (cursor != null) {
                cursor.moveToFirst();
                final String strBody = cursor.getString(cursor.getColumnIndex("body"));          // 在这里获取短信信息

                if(strBody != null && strBody.contains("京东")) {
                    Pattern continuousNumberPattern = Pattern.compile("(?<![0-9])([0-9]{6})(?![0-9])");
                    Matcher m = continuousNumberPattern.matcher(strBody);
                    String dynamicPassword = "";
                    while (m.find()) {
                        dynamicPassword = m.group();
                    }


                    String finalDynamicPassword = dynamicPassword;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpClient mOkHttpClient = new OkHttpClient();
                            try {
                                RequestBody requestBody = new FormBody.Builder().
                                        add("code", finalDynamicPassword).
                                        add("phoneNum1", phoneNum1).
                                        add("phoneNum2", phoneNum2).build();
                                Request request = new Request.Builder().url("https://dy.zhinianboke.com/jd/setCode").post(requestBody).build();
                                Response response = mOkHttpClient.newCall(request).execute();//发送请求
                                String result = response.body().string();
                                Log.d(TAG, "result: " + result);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    Log.i(TAG, "是我想要的短信" + dynamicPassword);
                }

            }else {
                Toast.makeText(MyService.this,"cursor 为 NULL",Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e) {
            Toast.makeText(MyService.this,"出错了::::"+e.getMessage(),Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        finally {
            Toast.makeText(MyService.this, String.valueOf(cursor != null),Toast.LENGTH_SHORT).show();
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    public class SMSContentObserver extends ContentObserver {
        private static final int MSG = 1;
        private int flag = 0;
        private Context mContext;
        private Handler mHandler;

        private Uri mUri = null;
        public SMSContentObserver(Context mContext,
                                  Handler mHandler) {
            super(mHandler);
            this.mContext = mContext;
            this.mHandler = mHandler;
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            // TODO Auto-generated method stub
            super.onChange(selfChange, uri);
            //onchange调用两次  过滤掉一次
            Log.i(TAG, "uri: "+ uri.toString());
            if (uri.toString().contains("content://sms/raw") || uri.toString().equals("content://sms") || uri.toString().equals("content://sms/inbox-insert")) {
                return;
            }
            setSmsCode();
        }
    }

}