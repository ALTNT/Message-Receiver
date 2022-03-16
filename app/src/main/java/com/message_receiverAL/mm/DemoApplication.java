package com.message_receiverAL.mm;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.text.Html.FROM_HTML_MODE_COMPACT;


/**
 * 1、为了打开客户端的日志，便于在开发过程中调试，需要自定义一个 Application。
 * 并将自定义的 application 注册在 AndroidManifest.xml 文件中。<br/>
 * 2、为了提高 push 的注册率，您可以在 Application 的 onCreate 中初始化 push。你也可以根据需要，在其他地方初始化 push。
 *
 * @author wangkuiwei
 */
public class DemoApplication extends Application {
    public static int isQqOnline = 1;
    public static int isWxOnline = 1;
    public static List<Integer> notifyIdswechat=new ArrayList<Integer>();
    public static List<Integer> notifyIdsQQ=new ArrayList<Integer>();
    public static List<Integer> notifyIdsOthers=new ArrayList<Integer>();
    public static SharedPreferences mySettings;
    public static SharedPreferences miSettings;
    private String alias=null;
    SQLiteOpenHelper openHelper;
    // user your appid the key.
    private static final String APP_ID = "";
    // user your appid the key.
    private static final String APP_KEY = "";
    final public static String qqColor="#1296DB";
    final public static String wxColor="#62B900";
    final public static String sysReceiveColor="#3F51B5";
    final public static String sysSendColor="#FF4081";

    // 此TAG在adb logcat中检索自己所需要的信息， 只需在命令行终端输入 adb logcat | grep
    // com.xiaomi.mipushdemo
    public static final String TAG = "com.message_receiverAL.mm";
    final public static String PREF = "com.message_receiverAL.mm_preferences";
    final public static String MYTAG = "GcmForMojo";
    final public static String QQ="Mojo-Webqq";
    final public static String WEIXIN="Mojo-Weixin";
    final public static String SYS="Mojo-Sys";
    final public static String KEY_TEXT_REPLY="key_text_reply";

    private static DemoHandler sHandler = null;
    private static MainActivity sMainActivity = null;

    private static DemoApplication myApp;
    private static Map<String, List<Spanned>> msgSave = new HashMap<>();
    private final Map<Integer, Integer> msgCountMap = new HashMap<>();
    private final Map<String, Integer> msgIdMap = new HashMap<>();
    private final ArrayList<User> currentUserList = new ArrayList<>();

    private final ArrayList<QqFriend> qqFriendArrayList = new ArrayList<>();
    private final ArrayList<QqFriendGroup> qqFriendGroups= new ArrayList<>();

    private final ArrayList<WechatFriend> WechatFriendArrayList = new ArrayList<>();
    private final ArrayList<WechatFriendGroup> WechatFriendGroups= new ArrayList<>();


    public static DemoApplication getInstance() {
        return myApp;
    }
    public Map<String, List<Spanned>> getMsgSave () {
        return this.msgSave;
    }
    public SQLiteOpenHelper getOpenHelper(){
        return this.openHelper;
    }

    public Map<Integer, Integer> getMsgCountMap () {
        return this.msgCountMap;
    }

    public Map<String, Integer> getMsgIdMap () {
        return this.msgIdMap;
    }

    public ArrayList<User> getCurrentUserList () {
        return this.currentUserList;
    }

    public ArrayList<QqFriend> getQqFriendArrayList () {
        return this.qqFriendArrayList;
    }

    public ArrayList<QqFriendGroup> getQqFriendGroups () {
        return this.qqFriendGroups;
    }

    public ArrayList<WechatFriend> getWechatFriendArrayList () {
        return this.WechatFriendArrayList;
    }

    public ArrayList<WechatFriendGroup> getWechatFriendGroups () {
        return this.WechatFriendGroups;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        initdb();
        try{
            readChat();
            if(ObjectSaveUtils.getHashSpanned(this,"keyHashSpanned")!=null)
                msgSave=ObjectSaveUtils.getHashSpanned(this,"keyHashSpanned");
        }catch (Exception e){
            e.printStackTrace();
        }
        myApp=this;
        miSettings = getSharedPreferences("mipush", Context.MODE_PRIVATE);
        mySettings = getSharedPreferences(PREF, Context.MODE_PRIVATE);

        // 注册push服务，注册成功后会向DemoMessageReceiver发送广播
        // 可以从DemoMessageReceiver的onCommandResult方法中MiPushCommandMessage对象参数中获取注册信息
        if (shouldInit()) {
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
            try{
                alias= (String)ObjectSaveUtils.getObject(DemoApplication.this,"savedalias");
            }catch (Exception e){
                e.printStackTrace();
            }
            if(alias!=null)
                MiPushClient.setAlias(this,alias,null);
        }

        //初始化通知分组（android O）及通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannelInit(R.string.notification_channel_sys_id,R.string.notification_channel_sys_name,R.string.notification_channel_sys_description, NotificationManager.IMPORTANCE_DEFAULT, Color.YELLOW);
            notificationChannelInit(R.string.notification_channel_qq_at_id,R.string.notification_channel_qq_at_name,R.string.notification_channel_qq_at_description,NotificationManager.IMPORTANCE_HIGH,Color.BLUE);
            notificationChannelInit(R.string.notification_channel_qq_discuss_id,R.string.notification_channel_qq_discuss_name,R.string.notification_channel_qq_discuss_descrption,NotificationManager.IMPORTANCE_DEFAULT,Color.BLUE);
            notificationChannelInit(R.string.notification_channel_qq_contact_id,R.string.notification_channel_qq_contact_name,R.string.notification_channel_qq_contact_descrption,NotificationManager.IMPORTANCE_HIGH,Color.BLUE);
            notificationChannelInit(R.string.notification_channel_qq_group_id,R.string.notification_channel_qq_group_name,R.string.notification_channel_qq_group_descrption,NotificationManager.IMPORTANCE_DEFAULT,Color.BLUE);
            notificationChannelInit(R.string.notification_channel_wechat_at_id,R.string.notification_channel_wechat_at_name,R.string.notification_channel_wechat_at_description,NotificationManager.IMPORTANCE_HIGH,Color.GREEN);
            notificationChannelInit(R.string.notification_channel_wechat_discuss_id,R.string.notification_channel_wechat_discuss_name,R.string.notification_channel_wechat_discuss_descrption,NotificationManager.IMPORTANCE_DEFAULT,Color.GREEN);
            notificationChannelInit(R.string.notification_channel_wechat_contact_id,R.string.notification_channel_wechat_contact_name,R.string.notification_channel_wechat_contact_descrption,NotificationManager.IMPORTANCE_HIGH,Color.GREEN);
            notificationChannelInit(R.string.notification_channel_wechat_group_id,R.string.notification_channel_wechat_group_name,R.string.notification_channel_wechat_group_descrption,NotificationManager.IMPORTANCE_DEFAULT,Color.GREEN);
            notificationGroupInit(R.string.notification_group_qq_id,R.string.notification_group_qq_name);
            notificationGroupInit(R.string.notification_group_wechat_id,R.string.notification_group_wechat_name);
            notificationGroupInit(R.string.notification_group_sys_id,R.string.notification_group_sys_name);
        }
        LoggerInterface newLogger = new LoggerInterface() {

            @Override
            public void setTag(String tag) {
                // ignore
            }

            @SuppressLint("LongLogTag")
            @Override
            public void log(String content, Throwable t) {
                Log.d(TAG, content, t);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void log(String content) {
                Log.d(TAG, content);
            }
        };
        Logger.setLogger(this, newLogger);
        if (sHandler == null) {
            sHandler = new DemoHandler(getApplicationContext());
        }
    }

    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        for (RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }
    public static String getCurTime(){

        SimpleDateFormat formatter    =   new    SimpleDateFormat    ("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date curDate    =   new    Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

    public static DemoHandler getHandler() {
        return sHandler;
    }

    public static void setMainActivity(MainActivity activity) {
        sMainActivity = activity;
    }

    public static class DemoHandler extends Handler {

        private Context context;

        public DemoHandler(Context context) {
            this.context = context;
        }

        public Context getContext() {
            return context;
        }

        @Override
        public void handleMessage(Message msg) {
            String s = (String) msg.obj;
            if (sMainActivity != null) {
                sMainActivity.refreshLogInfo();
            }
            if (!TextUtils.isEmpty(s)) {
                if(s.startsWith("@%^&")){
//                    Toast.makeText(context, s.substring(4), Toast.LENGTH_SHORT).show();
                    sMainActivity.openAPPWithString(s.substring(4));
                }
                else
                    Toast.makeText(context, s, Toast.LENGTH_LONG).show();
            }

        }
    }

    //首次运行时的通知渠道初始化
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void notificationChannelInit(int id_input, int name_input, int description_input, int importance_input,int color_input) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // 通知渠道的id
        String id = getString(id_input);
        // 用户可以看到的通知渠道的名字.
        CharSequence name = getString(name_input);
        // 用户可以看到的通知渠道的描述
        String description = getString(description_input);
        int importance = importance_input;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        // 配置通知渠道的属性
        mChannel.setDescription(description);
        // 设置通知出现时的闪灯（如果 android 设备支持的话）
        mChannel.enableLights(true);
        mChannel.setLightColor(color_input);
        mNotificationManager.createNotificationChannel(mChannel);
    }
    //通知分组初始化
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void notificationGroupInit(int group_id, int group_name) {
        // 通知渠道组的id.
        String group = getString(group_id);
        // 用户可见的通知渠道组名称.
        CharSequence name = getString(group_name);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannelGroup(new NotificationChannelGroup(group, name));
    }
    public static String getColorMsgTime(String messageType,Boolean isSend){

        String    str    =    "";
        switch(messageType){
            case SYS:
                if(!isSend) {
                    str    =    "<font color='"+sysReceiveColor+"'><small>"+ getCurTime()+"</small></font><br>";
                }else {
                    str    =    "<font color='"+sysSendColor+"'><small>"+ getCurTime()+"</small></font><br>";
                }
        }
        if(!isSend) {
            if(messageType.equals(QQ)){
                str    =    "<font color='"+qqColor+"'><small>"+ getCurTime()+"</small></font><br>";
            }else if(messageType.equals(WEIXIN)){
                str    =    "<font color='"+wxColor+"'><small>"+ getCurTime()+"</small></font><br>";
            }
        }else {
            if(messageType.equals(QQ)){
                str    =    "<font color='"+wxColor+"'><small>"+ getCurTime()+"</small></font><br>";
            }else if(messageType.equals(WEIXIN)){
                str    =    "<font color='"+qqColor+"'><small>"+ getCurTime()+"</small></font><br>";
            }
        }
        return str;
    }
    /**
     *  转换文字格式
     *
     *
     */

    public  static Spanned toSpannedMessage(String message){

        Spanned tmpMsg;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tmpMsg= Html.fromHtml(message,FROM_HTML_MODE_COMPACT);
        } else {
            //noinspection deprecation
            tmpMsg=Html.fromHtml(message);
        }

        return tmpMsg;

    }
    // 提取微信UID中的数字并进行字符数量削减，以兼容notifyID
    // created by Alex Wang at 20180205
    public static int WechatUIDConvert(String UID) {
        String str = UID;
        str=str.trim();
        String str2="";
        if(str != null && !"".equals(str)){
            for(int i=0;i<str.length();i++) {
                if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
                    str2 += str.charAt(i);
                }
            }
        }
        //针对系统账号进行优化，以防闪退
        switch (str) {
            case "newsapp":
                str2 = "639727700";
                break;
            case "filehelper":
                str2 = "345343573";
                break;
        }
        if(str2.length()==0){
            for(int i=0;i<str.length();i++) {
                str2+=str.charAt(i)%10+48;
            }
        }
        if(str2.length()>9)
            return Integer.parseInt(str2.substring(0,9));
        else
            return Integer.parseInt(str2);
    }
    private void initdb()
    {
        //准备数据库，存取聊天记录
        openHelper=new ChatSQLiteHelper(this,"chat.db",null,1) ;
    }
    public void readChat(){
        String _text;
        //获取数据库中的信息
        SQLiteDatabase db=openHelper.getReadableDatabase();
//        String sql="select contentChat from chat where friend_id=?";
        String sql="select contentChat from chat";
        String sql2="select friend_id from chat";
//        Cursor c = db.rawQuery(sql,new String[]{friend_id});
        Cursor c = db.rawQuery(sql, null);
        while(c.moveToNext()){
            _text=c.getString(0);
            Log.v("ceshi", _text);
        }
        Cursor c2 = db.rawQuery(sql2, null);
        while(c2.moveToNext()){
            _text=c2.getString(0);
            Log.v("ceshi2", _text);
        }
        db.close();
    }
}