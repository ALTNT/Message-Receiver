package com.message_receiverAL.mm;
//
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;



public class WhiteService extends Service {
    private Timer timer = new Timer(true);
    private Handler handler  = new Handler(){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what== 1){
                //todo something....
//                loginBjtuWifi();//登录北京交通大学校园网
            }
        }
    };
    //任务
    private TimerTask task = new TimerTask() {
        public void run() {
            try{
                Message msg = new Message();
                msg.what=1;
                handler.sendMessage(msg);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    private static final String TAG = WhiteService.class.getSimpleName();
    private static final int NOTIFICATION_FLAG =0X11;
    private static final String CHANNEL_ONE_ID = "NOTIFY_ID";
    private static final String CHANNEL_ONE_NAME = "PUSH_NOTIFY_NAME";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        task.cancel();
        task = new TimerTask() {
            public void run() {
                try{
                    Message msg = new Message();
                    msg.what=1;
                    handler.sendMessage(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(task, 0, 3*60*1000);
        // 在Android进行通知处理，首先需要重系统哪里获得通知管理器NotificationManager，它是一个系统Service。
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 设置点击通知跳转的Intent
        Intent nfIntent = new Intent(this, MainActivity.class);
        // 设置 延迟Intent
        // 最后一个参数可以为PendingIntent.FLAG_CANCEL_CURRENT 或者 PendingIntent.FLAG_UPDATE_CURRENT
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nfIntent, 0);

        //构建一个Notification构造器
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());

        //适配8.0service
        NotificationChannel mChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(mChannel);
            builder.setChannelId(CHANNEL_ONE_ID);
        }

        builder.setContentIntent(pendingIntent)   // 设置点击跳转界面
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        R.mipmap.bubble)) // 设置下拉列表中的图标(大图标)
                .setTicker("您有一个notification")// statusBar上的提示
                .setContentTitle("mi消息接收 正在后台运行") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.bubble) // 设置状态栏内的小图标24X24
//                .setContentText("为了能正常收到通知，请不要结束应用。") // 设置详细内容
                .setContentIntent(pendingIntent) // 设置点击跳转的界面
                .setWhen(System.currentTimeMillis()) // 设置该通知发生的时间
                .setDefaults(Notification.DEFAULT_VIBRATE) //默认震动方式
                .setPriority(Notification.PRIORITY_HIGH);   //优先级高

        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        notification.flags |= Notification.FLAG_AUTO_CANCEL; // FLAG_AUTO_CANCEL表明当通知被用户点击时，通知将被清除。
        notification.flags |= Notification.FLAG_ONGOING_EVENT; //将此通知放到通知栏的"Ongoing"即"正在运行"组中
        notification.flags |= Notification.FLAG_NO_CLEAR; //表明在点击了通知栏中的"清除通知"后，此通知不清除，常与FLAG_ONGOING_EVENT一起使用


        //manager.notify(NOTIFICATION_FLAG, notification);
        // 启动前台服务
        // 参数一：唯一的通知标识；参数二：通知消息。
        startForeground(NOTIFICATION_FLAG, notification);// 开始前台服务
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 停止前台服务--参数：表示是否移除之前的通知
        stopForeground(true);
        Log.d(TAG, "onDestroy");
    }

}