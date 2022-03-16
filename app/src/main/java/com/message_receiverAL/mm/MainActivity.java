package com.message_receiverAL.mm;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import xyz.kumaraswamy.autostart.Autostart;

import static com.message_receiverAL.mm.DemoApplication.SYS;
import static com.message_receiverAL.mm.DemoApplication.getCurTime;
import static com.message_receiverAL.mm.DemoApplication.mySettings;

//import static com.message_receiverAL.mm.DemoApplication.deviceGcmToken;
//import static com.message_receiverAL.mm.DemoApplication.deviceMiToken;
/**
 * 1、本 demo 可以直接运行，设置 topic 和 alias。
 * 服务器端使用 appsecret 即可以向demo发送广播和单点的消息。<br/>
 * 2、为了修改本 demo 为使用你自己的 appid，你需要修改几个地方：DemoApplication.java 中的 APP_ID,
 * APP_KEY，AndroidManifest.xml 中的 packagename，和权限 permission.MIPUSH_RECEIVE 的前缀为你的 packagename。
 *
 * @author wangkuiwei
 */
public class MainActivity extends AppCompatActivity {
    public ListView currentUserListView;
    public UserAdapter currentUserAdapter;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    final private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private ArrayList<User> currentUserList;
    public static List<String> logList = new CopyOnWriteArrayList<String>();

    private Intent intent_service=null;
    private TextView mLogView = null;
    public static Handler userHandler;
    // user your appid the key.
    private static final String APP_ID = "";
    // user your appid the key.
    private static final String APP_KEY = "";
    private static MainActivity myactivity=null;
    public static MainActivity getInstance() {
        return myactivity;
    }

    // 此TAG在adb logcat中检索自己所需要的信息， 只需在命令行终端输入 adb logcat | grep
    // com.xiaomi.mipushdemo
    public static final String TAG = "com.message_receiverAL.mm";
//启动定时器
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        if (android.os.Build.VERSION.SDK_INT > 9) {
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//        }
        super.onCreate(savedInstanceState);
        clearWhiteList();//通过黑阈的api接口使用adb命令将国内某些毒瘤软件（例如wx 、qq）移出白名单

        myactivity=this;
        setContentView(R.layout.activity_main);
        getOverflowMenu();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DemoApplication.setMainActivity(this);
        if(!SystemUtil.isSystemWhiteList(MainActivity.this)){
            requestIgnoreBatteryOptimizations();
        }

        currentUserList = DemoApplication.getInstance().getCurrentUserList();
        verifyStoragePermissions(this);

        userHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                String handlerMsg = (String)msg.obj;

                if(handlerMsg.equals("UpdateCurrentUserList") && currentUserAdapter!=null){
                    currentUserAdapter.notifyDataSetChanged();
                }

                super.handleMessage(msg);
            }
        };

        //SharedPreferences Settings = getSharedPreferences(PREF, Context.MODE_PRIVATE);
        final String qqReplyUrl=mySettings.getString("edit_text_preference_qq_replyurl","");
        final String wxReplyUrl=mySettings.getString("edit_text_preference_wx_replyurl","");
        final  String qqPackgeName=mySettings.getString("edit_text_preference_qq_packgename","com.tencent.mobileqq");
        final  String wxPackgeName=mySettings.getString("edit_text_preference_wx_packgename","com.tencent.mm");


        currentUserListView = (ListView) findViewById(R.id.current_user_list_view);
        addNotfiyContent();//点击通知增加会话内容，用于缓存被杀时列表内容为空的情况，使用通知自带的最后一条消息
        currentUserAdapter = new UserAdapter(MainActivity.this,R.layout.current_userlist_item,currentUserList);
        currentUserListView.setAdapter(currentUserAdapter);
        currentUserListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        currentUserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                User p=(User) parent.getItemAtPosition(position);
                Intent intentSend = new Intent(getApplicationContext(), DialogActivity.class);
                intentSend.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                Bundle msgDialogBundle = new Bundle();
                msgDialogBundle.putString("msgId",p.getUserId());
                msgDialogBundle.putString("qqReplyUrl",qqReplyUrl);
                msgDialogBundle.putString("wxReplyUrl",wxReplyUrl);
                msgDialogBundle.putString("senderType", p.getSenderType());
                msgDialogBundle.putString("msgType",p.getUserType());
                msgDialogBundle.putString("msgTitle",p.getUserName());
                msgDialogBundle.putString("msgBody",p.getUserMessage());
                msgDialogBundle.putInt("notifyId", p.getNotifyId());
                msgDialogBundle.putString("msgTime",p.getUserTime());
                msgDialogBundle.putString("qqPackgeName",qqPackgeName);
                msgDialogBundle.putString("wxPackgeName",wxPackgeName);
                intentSend.putExtras(msgDialogBundle);

                startActivity(intentSend);
            }
        });

        currentUserListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent,View view,int position,long id) {

                //系统信息不能删除
                if(!currentUserList.get(position).getUserId().equals("0") && !currentUserList.get(position).getUserId().equals("1") && !currentUserList.get(position).getUserId().equals("2")) {
                    currentUserList.remove(position);
                    currentUserAdapter.notifyDataSetChanged();
                }
                return true;//当返回true时,不会触发短按事件
                //return false;//当返回false时,会触发短按事件
            }
        });
        //开启前台服务
        intent_service = new Intent(MainActivity.this, WhiteService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {//8.0以上的开启方式不同
            startForegroundService(intent_service);
        } else {
            startService(intent_service);
        }
//        if(!isAllowAutoStart(MainActivity.this,"com.message_receiverAL.mm")){
//            MobileButlerUtil moBuUtil_tmp=new MobileButlerUtil(MainActivity.this);
//            moBuUtil_tmp.goXiaomiSetting();
//        }
        // make sure device is MIUI device, else an
// exception will be thrown at initialization
        Autostart autostart = null;
        try {
            autostart = new Autostart(getContext());
            Autostart.State state = autostart.getAutoStartState();

            if (state == Autostart.State.DISABLED) {
                // now we are sure that autostart is disabled
                // ask user to enable it manually in the settings app
                goXiaomiSetting();
            } else if (state == Autostart.State.ENABLED) {
                // now we are also sure that autostart is enabled
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        mLogView = (TextView) findViewById(R.id.log);

    }
    private void goXiaomiSetting() {
        String manufacturer = "xiaomi";
        if (manufacturer.equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
            //this will open auto start screen where user can enable permission for your app
            Intent intent1 = new Intent();
            intent1.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            startActivity(intent1);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isIgnoring;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isWechatIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations("com.tencent.mm");
        }
        return isIgnoring;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void clearWhiteList(){//清除毒瘤软件的白名单
        if(isWechatIgnoringBatteryOptimizations()){
            final String action = "me.piebridge.brevent.intent.action.COMMAND";
            final String extra = "me.piebridge.brevent.intent.extra.COMMAND";

            List<String> commands = new ArrayList<>();
            commands.add("dumpsys deviceidle whitelist -com.tencent.mm");
            for(String command : commands){
                Intent intent = new Intent(action);
                intent.putExtra(extra, command);

                // 不建议指定包名，黑阈编译版包名与发布版不一样
                List<ResolveInfo> ris = getPackageManager().queryIntentActivities(intent, 0);
                startActivity(intent);
                if (ris != null && !ris.isEmpty()) {
                    startActivity(intent);
                }
            }
        }
    }

    //显示menu——main
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_qq_contacts:
                break;
            case R.id.action_wechat_contacts:
                break;
            case R.id.action_settings:
                Intent intent = new Intent();
                intent.setClass(this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.action_help:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
//        onStart()通常就是onStop()(用户按下home键，activity变为后台)之后用户再切回这个activity就会调用onRestart()然后调用onStart();
//        onResume()是onPaused()（activity被另一个透明或者Dialog样式的activity覆盖了）之后dialog取消，activity回到可交互状态，调用onResume();
        super.onResume();
        clearWhiteList();
        refreshLogInfo();

        //进入界面清除通知
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        try{
            ArrayList<User> list_tmp=(ArrayList<User>)ObjectSaveUtils.getObject(getContext(),"currentUserList");
            if(list_tmp!=null){
                for(User s: list_tmp){
                    if(!isHaveMsg(currentUserList,s.getUserId()))
                        currentUserList.add(s);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //由于机器人接受消息后会变成SYS状态，此处直接进行更改，并在MyApplication内做相应处理
        if (!isHaveMsg(currentUserList,"2"))
            currentUserList.add(new User(getString(R.string.user_bot_wechat_name), "2", SYS, getString(R.string.user_bot_msg), getCurTime(), "1", 2, "0"));
        if (!isHaveMsg(currentUserList,"1"))
            currentUserList.add(new User(getString(R.string.user_bot_qq_name), "1", SYS, getString(R.string.user_bot_msg), getCurTime(), "1", 1, "0"));
        if (!isHaveMsg(currentUserList,"0"))
            currentUserList.add(new User(getString(R.string.user_welcome_name), "0", SYS, getString(R.string.user_welcome_msg), getCurTime(), "1", 0, "0"));
//        DemoApplication.read_msg();
//        //更新会话列表
//        if (MainActivity.userHandler != null)
//            new userThread().start();
//        //更新聊天对话框
//        if (DialogActivity.msgHandler != null)
//            new MsgThread().start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DemoApplication.setMainActivity(null);
//        //关闭前台服务
//        stopService(intent_service);
    }

    public void refreshLogInfo() {
        String AllLog = "";
        for (String log : logList) {
            AllLog = AllLog + log + "\n\n";
        }
        mLogView.setText(AllLog);
    }

    public void openAPPWithString(String s){
//        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        if(s.startsWith("QQ")){
            if(!openAPP("com.tencent.tim","com.tencent.mobileqq.activity.SplashActivity"))
                openAPP("com.tencent.mobileqq","com.tencent.mobileqq.activity.SplashActivity");
        }
        else if(s.startsWith("微信")){
            openAPP("com.tencent.mm","com.tencent.mm.ui.LauncherUI");
        }
//        String wifiName=getConnectWifiSsid();
//        Log.d("SSID------", wifiName);
////        if(wifiName.equalsIgnoreCase("local.bjtu.edu.cn")||wifiName.equalsIgnoreCase("web.bjtu.edu.cn")||wifiName.equalsIgnoreCase("phone.bjtu.edu.cn")){
//        loginBjtuWifi();
//        }
    }



    //获取手机当前链接的wifi名称
    private String getConnectWifiSsid(){
        WifiManager wifiManager = (WifiManager)getApplicationContext(). getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.d("wifiInfo------", wifiInfo.toString());

        String wifiInfo1=wifiInfo.getBSSID().replace("\"","").replace("\"","");
        Log.d("wifiwtf",wifiInfo1);
        return wifiInfo1;
    }
    public boolean openAPP(String pkg,String cls) {//pkg为包名，cls为启动类名
        try{
            Intent intent=new Intent(Intent.ACTION_MAIN);
            ComponentName cmp=new ComponentName(pkg,cls);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cmp);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            String uriString = intent.toUri(Intent.URI_INTENT_SCHEME);
//            Toast.makeText(this, uriString, Toast.LENGTH_LONG).show();
//            Log.d("jkfk",uriString);
            startActivity(intent);
            return true;
        }catch (Exception e){
            Log.e("打开另外一个应用出错",e.getMessage());   //未打开，可能要打开的app没有安装，需要再此进行处理
            Toast.makeText(this, "打开另外一个应用出错"+e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }

    }

    public void requestIgnoreBatteryOptimizations() {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
            startActivityForResult(intent,1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Context getContext() {
        return MainActivity.this;
    }

//
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    private boolean isIgnoringBatteryOptimizations() {
//        boolean isIgnoring = false;
//        PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
//        if (powerManager != null) {
//            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getContext().getPackageName());
//        }
//        return isIgnoring;
//    }
    /**
     * force to show overflow menu in action bar (phone) by
     * http://blog.csdn.net/jdsjlzx/article/details/36433441
     */
    public void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    public  Boolean  isHaveMsg(final ArrayList<User> userList,final String userId){
        if(userList.size()==0) {
            return false;
        }
        for(int    i=0;    i<userList.size();    i++){
            String str = userList.get(i).getUserId();
            if(str.equals(userId)){
                return true;
            }
        }
        return false;
    }
    public void addNotfiyContent() {
        //点击通知增加会话内容，用于缓存被杀时列表内容为空的情况，使用通知自带的最后一条消息
        Intent intentCurrentListUser = getIntent();/** Return the intent that started this activity. */
        if(intentCurrentListUser!=null) {
//            String msgsdfsa=new String(String.valueOf(currentUserList.size()));
//            Log.d("intisnot",msgsdfsa+intentCurrentListUser.toString());
            Bundle msgBundle = intentCurrentListUser.getExtras();
//            Log.d("intis", String.valueOf(msgBundle.containsKey("userId")));
            if (msgBundle != null && msgBundle.containsKey("userId")) {

                if (msgBundle.getInt("notifyId") != -1) {
                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(msgBundle.getInt("notifyId"));
                 /*   for(int    i=0;    i<currentUserList.size();    i++){
                        if(currentUserList.get(i).getNotifyId()==notifyId){
                            currentUserList.get(i).setMsgCount("0");
                            if(CurrentUserActivity.userHandler!=null)
                                new WeixinNotificationBroadcastReceiver.userThread().start();
                            break;
                        }
                    }
                    */

                }

                if (!isHaveMsg(currentUserList, msgBundle.getString("userId"))) {
                    User noifyMsg = new User(msgBundle.getString("userName"), msgBundle.getString("userId"), msgBundle.getString("userType"), msgBundle.getString("userMessage"), msgBundle.getString("userTime"), msgBundle.getString("senderType"), msgBundle.getInt("NotificationId"), msgBundle.getString("msgCount"));
                    currentUserList.add(0, noifyMsg);

                    if (currentUserAdapter != null) {
                        currentUserAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

}

