package com.message_receiverAL.mm;


import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.wenming.library.BackgroundUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static android.app.Notification.DEFAULT_LIGHTS;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.USAGE_STATS_SERVICE;
import static com.message_receiverAL.mm.DemoApplication.KEY_TEXT_REPLY;
import static com.message_receiverAL.mm.DemoApplication.MYTAG;
import static com.message_receiverAL.mm.DemoApplication.PREF;
import static com.message_receiverAL.mm.DemoApplication.QQ;
import static com.message_receiverAL.mm.DemoApplication.SYS;
import static com.message_receiverAL.mm.DemoApplication.WEIXIN;
import static com.message_receiverAL.mm.DemoApplication.WechatUIDConvert;
import static com.message_receiverAL.mm.DemoApplication.getColorMsgTime;
import static com.message_receiverAL.mm.DemoApplication.getCurTime;
import static com.message_receiverAL.mm.DemoApplication.isQqOnline;
import static com.message_receiverAL.mm.DemoApplication.isWxOnline;
import static com.message_receiverAL.mm.DemoApplication.mySettings;
import static com.message_receiverAL.mm.DemoApplication.toSpannedMessage;


/**
 *
 * @author heipidage
 * ??????????????????
 */

public class MessageUtil {


    public static void  MessageUtilDo(Context context ,String msgId,String msgType,String senderType,String msgTitle,String msgBody,String msgIsAt) {


        Map<String, List<Spanned>> msgSave;
        Map<Integer, Integer> msgCountMap;
        Map<String, Integer> msgIdMap;
        ArrayList<User> currentUserList;
        SQLiteOpenHelper openHelper;


        msgSave = DemoApplication.getInstance().getMsgSave();
        msgCountMap = DemoApplication.getInstance().getMsgCountMap();
        msgIdMap = DemoApplication.getInstance().getMsgIdMap();
        currentUserList = DemoApplication.getInstance().getCurrentUserList();
        openHelper = DemoApplication.getInstance().getOpenHelper();
        //SharedPreferences mySettings = context.getSharedPreferences(PREF, MODE_PRIVATE);


        int notifyId;
        int msgCount;


        if (msgId == null) msgId = "0"; //??????????????????
        if (senderType == null) senderType = "1"; //?????????????????? ???????????????
        if(msgIsAt == null) msgIsAt = "0"; //???????????????????????????@??????

        if(msgType.equals(QQ) ) { //???????????????QQ????????????????????????????????????????????????
            isQqOnline=1;
        } else if ( msgType.equals(WEIXIN)) {
            isWxOnline = 1;
        }

        //????????????????????????
        //??????msgId????????????id?????????hashmap???????????????
        if (msgIdMap.get(msgId) == null)
        {
            String s=java.net.URLEncoder.encode(msgId);
            try{
                if(s.length()>20)
                    s=s.substring(0,20);
                int ss=WechatUIDConvert(s);
            }catch (Exception e){
                Log.d("cnmnmsl",e.toString());
            }
            notifyId=WechatUIDConvert(s);

//            switch (msgType) {
//                case QQ:
//                    if (msgId.length() > 9) {
//                        notifyId = Integer.parseInt(msgId.substring(0, 9));
//                    } else {
//                        notifyId = Integer.parseInt(msgId);
//                    }
//                    break;
//                case WEIXIN:
//                    notifyId = WechatUIDConvert(msgId);
//                    break;
//                case SYS:
//                    notifyId = Integer.parseInt(msgId); //QQ?????????1??????????????????2
//                    break;
//                default:
//                    notifyId = 0; //????????????????????????Id?????????0
//            }

            msgIdMap.put(msgId, notifyId); //??????msgIdMap
        } else
        {
            notifyId = msgIdMap.get(msgId);
        }

        //??????????????????
        if (msgCountMap.get(notifyId) == null || msgCountMap.get(notifyId).equals(0))
        {
            msgCount = 1;
        } else
        {
            msgCount = msgCountMap.get(notifyId) + 1;
        }
        if (DialogActivity.notifyId!=null&&DialogActivity.notifyId == notifyId)
        {  //????????????????????????id?????????id??????????????????????????????0
            msgCount = 0;
        }
        msgCountMap.put(notifyId, msgCount);

        //????????????????????????????????????????????????????????????,???????????????????????????
        User currentUser = new User(msgTitle, msgId, msgType, msgBody, getCurTime(), senderType, notifyId, String.valueOf(msgCount));
        for (int i = 0; i < currentUserList.size(); i++)
        {
            if (currentUserList.get(i).getUserId().equals(msgId))
            {
                currentUserList.remove(i);
                break;
            }
        }
        currentUserList.add(0, currentUser);
        try{
            ObjectSaveUtils.saveObject(context,"currentUserList",currentUserList);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (MainActivity.userHandler != null)
            new userThread().start();


        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        Spanned spannedMessage = toSpannedMessage(getColorMsgTime(msgType, false) + msgBody);

        if (msgSave.get(msgId) == null) {

            List<Spanned> msgList = new ArrayList<>();
            msgList.add(spannedMessage);
            msgSave.put(msgId, msgList);
            //??????????????????????????????
            SQLiteDatabase db=openHelper.getReadableDatabase();
            ContentValues values = new ContentValues();
            values.put("friend_id",msgId);
            values.put("contentChat", Html.toHtml(spannedMessage));
            db.insert("chat",null,values);
            db.close();

        } else {

            List<Spanned> msgList = msgSave.get(msgId);
            msgList.add(spannedMessage);
            msgSave.put(msgId, msgList);

            //??????????????????????????????
            SQLiteDatabase db=openHelper.getReadableDatabase();
            ContentValues values = new ContentValues();
            values.put("friend_id",msgId);
            values.put("contentChat", Html.toHtml(spannedMessage));
            db.insert("chat",null,values);
            db.close();

        }
        try{
            DemoApplication.getInstance().readChat();
            ObjectSaveUtils.saveHashSpanned(context,"keyHashSpanned",msgSave);
//        ObjectSaveUtils.saveObject(context,"key",msgSave);
//        msgSave=null;
//        msgSave=(Map<String, List<Spanned>>)ObjectSaveUtils.getObject(context,"key");
        }catch (Exception e){
            e.printStackTrace();
        }

        //?????????????????????????????????handler???????????????ui??????
        if (DialogActivity.msgHandler != null){
            new MsgThread().start();
        }

        //??????????????????
        String qqReciveType = mySettings.getString("qq_list_preference_1", "1");
        String wxReciveType = mySettings.getString("wx_list_preference_1", "1");
        Boolean qqIsDetail = mySettings.getBoolean("check_box_preference_qq_detail", true);
        Boolean wxIsDetail = mySettings.getBoolean("check_box_preference_wx_detail", true);
        String qqSound = mySettings.getString("ringtone_preference_qq", "");
        String wxSound = mySettings.getString("ringtone_preference_wx", "");
        String qqVibrate = mySettings.getString("qq_list_preference_vibrate", "1");
        String wxVibrate = mySettings.getString("wx_list_preference_vibrate", "1");
        Boolean qqIsReciveGroup = mySettings.getBoolean("check_box_preference_qq_isReciveGroup", true);
        Boolean wxIsReciveGroup = mySettings.getBoolean("check_box_preference_wx_isReciveGroup", true);

        String qqPackgeName = mySettings.getString("edit_text_preference_qq_packgename", "com.tencent.mobileqq");
        String wxPackgeName = mySettings.getString("edit_text_preference_wx_packgename", "com.tencent.mm");


        //  ????????????????????????????????????????????????????????? ???????????????
        if (DialogActivity.notifyId==null ||DialogActivity.notifyId == notifyId)
        {
            return;
        }

        //??????????????????????????????
        if (msgType.equals(QQ))
        {
            long time = new Date().getTime();
            long paused_time = context.getSharedPreferences("paused_time", MODE_PRIVATE).getLong("paused_time", 0);

            if (!mySettings.getBoolean("check_box_preference_qq", false))
            { //????????????
                return;
            } else if (time < paused_time)
            { //????????????
                return;
            }

            //???????????????????????????
            if (!qqIsReciveGroup)
            {
                if (senderType.equals("2") || senderType.equals("3"))
                {
                    if(msgIsAt.equals("0")) //??????????????????At?????????
                        return;
                }
            }

            //??????????????????
            //QQ??????
            switch (qqReciveType) {
                case "1":
                    Log.d(MYTAG, "QQ????????????????????????");
                    break;
                case "2":
                    Boolean isForeground;
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                    {

                        isForeground = queryAppUsageStats(context, qqPackgeName);
                        if (isForeground)
                        {
                            Log.d(MYTAG, "QQ??????????????????");
                            return;
                        }

                    } else if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
                    {

                        isForeground = BackgroundUtil.getRunningTask(context, qqPackgeName);
                        if (isForeground)
                        {
                            Log.d(MYTAG, "QQ??????????????????");
                            return;
                        }

                    } else
                    { //???5.0 API 21 ????????????

                        isForeground = BackgroundUtil.getLinuxCoreInfo(context, qqPackgeName);
                        if (isForeground)
                        {
                            Log.d(MYTAG, "QQ??????????????????");
                            return;
                        }

                    }
                    break;
                case "3":
                    if (isServiceRunning(context, qqPackgeName))
                    {
                        Log.d(MYTAG, "QQ??????????????????");
                        return;
                    }
                    break;
                case "4":
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(USAGE_STATS_SERVICE);
                        if (!usageStatsManager.isAppInactive(qqPackgeName))
                        {
                            Log.d(MYTAG, "QQ??????????????????");
                            return;
                        }
                    } else
                    {
                        Looper.prepare();
                        Toast.makeText(context.getApplicationContext(), R.string.toast_check_doze_fail, Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                    break;
            }
        }

        //????????????

        if (msgType.equals(WEIXIN))
        {

            if (!mySettings.getBoolean("check_box_preference_wx", false))
            { //????????????
                return;
            }

            //???????????????????????????
            if (!wxIsReciveGroup)
            {
                if (senderType.equals("2") || senderType.equals("3"))
                {
                    if(msgIsAt.equals("0")) //??????????????????At?????????
                        return;
                }
            }

            switch (wxReciveType) {
                case "1":
                    Log.d(MYTAG, "??????????????????????????????");
                    break;
                case "2":
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                    {

                        Boolean isForeground = queryAppUsageStats(context, wxPackgeName);
                        if (isForeground)
                        {
                            Log.d(MYTAG, "????????????????????????");
                            return;
                        }

                    } else if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
                    {

                        Boolean isForeground = BackgroundUtil.getRunningTask(context, wxPackgeName);
                        if (isForeground)
                        {
                            Log.d(MYTAG, "????????????????????????");
                            return;
                        }
                    } else
                    {

                        Boolean isForeground = BackgroundUtil.getLinuxCoreInfo(context, wxPackgeName);
                        if (isForeground)
                        {
                            Log.d(MYTAG, "????????????????????????");
                            return;
                        }
                    }
                    break;
                case "3":
                    if (isServiceRunning(context, wxPackgeName))
                    {
                        Log.d(MYTAG, "????????????????????????");
                        return;
                    }
                    break;
                case "4":

                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(USAGE_STATS_SERVICE);

                        if (!usageStatsManager.isAppInactive(wxPackgeName))
                        {
                            Log.d(MYTAG, "????????????????????????");
                            return;
                        }
                    } else
                    {
                        Looper.prepare();
                        Toast.makeText(context.getApplicationContext(), R.string.toast_check_doze_fail, Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                    break;
            }
        }


        //????????????
        switch (msgType) {
            case QQ:
                if (!qqIsDetail)
                {
                    msgTitle = context.getString(R.string.notification_title_default);
                    msgBody = context.getString(R.string.notification_detail_default);
                }
                sendNotificationQq(context, msgTitle, msgBody, notifyId, msgCount, qqSound, qqVibrate, msgId, senderType, qqPackgeName, msgIsAt);
                break;
            case WEIXIN:
                if (!wxIsDetail)
                {
                    msgTitle = context.getString(R.string.notification_title_default);
                    msgBody = context.getString(R.string.notification_detail_default);
                }
                sendNotificationWx(context, msgTitle, msgBody, notifyId, msgCount, wxSound, wxVibrate, msgId, senderType, wxPackgeName, msgIsAt);
                break;
            case SYS:
                if (msgTitle.contains(context.getString(R.string.text_login_qrcode)))
                {
                    try {
                        download(context, msgBody);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                //??????????????????
                if (msgTitle.contains(context.getString(R.string.text_login_qrcode_scan)))
                {
                    if (msgId.equals("1"))
                    {
                        isQqOnline = 0;
                    } else if (msgId.equals("2"))
                    {
                        isWxOnline = 0;
                    }
                }

                if (msgBody.contains(context.getString(R.string.text_login_success)))
                {
                    if (msgId.equals("1")) //QQ????????????
                    {
                        isQqOnline = 1;
                        //??????????????????
                        for (int i = 0; i < currentUserList.size(); i++)
                        {
                            if (currentUserList.get(i).getUserType().equals(QQ))
                            {
                                currentUserList.remove(i);
                            }
                        }
                        //??????????????????
                        DemoApplication.getInstance().getQqFriendArrayList().clear();
                        DemoApplication.getInstance().getQqFriendGroups().clear();
                        //??????????????????
                        for (Object o : msgSave.entrySet()) {
                            String key = o.toString();
                            if (key.length() == 10)  //QQ???key??????msgId???10???
                                msgSave.remove(key);
                        }
                    } else if (msgId.equals("2")) //??????????????????
                    {
                        isWxOnline = 1;
                        //??????????????????
                        for (int i = 0; i < currentUserList.size(); i++)
                        {
                            if (currentUserList.get(i).getUserType().equals(WEIXIN))
                            {
                                currentUserList.remove(i);
                            }
                        }
                        //??????????????????
                        //??????????????????
                        for (Object o : msgSave.entrySet()) {
                            String key = o.toString();
                            if (key.length() > 10)  //?????????key??????msgId??????10???
                                msgSave.remove(key);
                        }
                    }
                    //?????????????????????
                    Log.i(MYTAG, "onMessageReceived: ?????????????????????");
                    File file = new File(Environment.getExternalStorageDirectory() + "/GcmForMojo/");
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    File[] childFiles = file.listFiles();
                    for (File temp : childFiles)
                    {
                        Log.d(MYTAG, "onMessageReceived: delete: " + temp.getAbsolutePath());
                        //noinspection ResultOfMethodCallIgnored
                        temp.delete();
                    }
                    //??????????????????
                    if (MainActivity.userHandler != null)
                        new userThread().start();
                    //?????????????????????
                    if (DialogActivity.msgHandler != null)
                        new MsgThread().start();
                }

                //??????????????????
                sendNotificationSys(context, msgTitle, msgBody, msgId, notifyId, msgCount);
                break;
        }

    }

    //qq????????????
    private static void sendNotificationQq(Context context, String msgTitle, String msgBody, int notifyId, int msgCount, String qqSound, String qqVibrate, String msgId,
                                           String senderType, String qqPackgeName, String msgIsAt)
    {
        SharedPreferences mySettings = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        String qqNotifyClick = mySettings.getString("qq_notify_click","1");

        Bundle msgNotifyBundle = new Bundle();
        msgNotifyBundle.putInt("notifyId", notifyId);
        msgNotifyBundle.putString("qqPackgeName", qqPackgeName);

        //??????????????????(?????????)
        Intent intentCancel = new Intent(context,QqNotificationBroadcastReceiver.class);
        intentCancel.setAction("qq_notification_cancelled");
        intentCancel.putExtras(msgNotifyBundle);
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(context, notifyId, intentCancel, PendingIntent.FLAG_UPDATE_CURRENT);

        //?????????????????? by Mystery0
        // Intent intentPause = new Intent(this, QqPausedNotificationReceiver.class);
        // intentPause.setAction("qq_notification_paused");
        // intentPause.putExtras(msgNotifyBundle);
        // PendingIntent pendingIntentPause = PendingIntent.getBroadcast(this, notifyId, intentPause, PendingIntent.FLAG_UPDATE_CURRENT);

        //???????????? ?????????????????????????????????????????????????????????
        Intent intentList = new Intent(context, MainActivity.class);
        intentList.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle msgListBundle = new Bundle();
        msgListBundle.putString("userName", msgTitle);
        msgListBundle.putString("userId", msgId);
        msgListBundle.putString("userType", QQ);
        msgListBundle.putString("userMessage", msgBody);
        msgListBundle.putString("userTime", getCurTime());
        msgListBundle.putString("senderType", senderType);
        msgListBundle.putInt("notifyId", notifyId);
        msgListBundle.putString("msgCount", String.valueOf(msgCount));
        intentList.putExtras(msgListBundle);
        PendingIntent pendingIntentList = PendingIntent.getActivity(context, notifyId, intentList, PendingIntent.FLAG_UPDATE_CURRENT);

        //qq??????(?????????)
        Intent intentQq = new Intent(context, QqNotificationBroadcastReceiver.class);
        intentQq.setAction("qq_notification_clicked");
        intentQq.putExtras(msgNotifyBundle);
        PendingIntent pendingIntentQq = PendingIntent.getBroadcast(context, notifyId, intentQq, PendingIntent.FLAG_ONE_SHOT);

        //????????????
        Intent intentDialog = new Intent(context, DialogActivity.class);

        Bundle msgDialogBundle = new Bundle();
        msgDialogBundle.putString("msgId", msgId);
        msgDialogBundle.putString("senderType", senderType);
        msgDialogBundle.putString("msgType", QQ);
        msgDialogBundle.putString("msgTitle", msgTitle);
        msgDialogBundle.putString("msgBody", msgBody);
        msgDialogBundle.putInt("notifyId", notifyId);
        msgDialogBundle.putString("msgTime", getCurTime());
        msgDialogBundle.putString("qqPackgeName", qqPackgeName);
        msgDialogBundle.putString("fromNotify", "1");

        intentDialog.putExtras(msgDialogBundle);
        intentDialog.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntentDialog = PendingIntent.getActivity(context, notifyId, intentDialog, PendingIntent.FLAG_UPDATE_CURRENT);

        StringBuffer ticker = new StringBuffer();
        ticker.append(msgTitle);
        ticker.append("\r\n");
        ticker.append(msgBody);

        if (msgCount != 1)
        {
            msgTitle = msgTitle + "(" + msgCount +context.getString(R.string.notify_title_msgcount_new) +")";
        }

        Uri defaultSoundUri = Uri.parse(qqSound);

        NotificationCompat.Builder notificationBuilder = null;
        //??????API????????????????????????????????????
        notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.qq_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.qq))
                .setTicker(ticker)
                .setContentTitle(msgTitle)
                .setContentText(msgBody)
                .setStyle(new NotificationCompat.BigTextStyle() // ???????????????????????????????????????
                        .bigText(msgBody))
                //   .setSubText(context.getString(R.string.notification_group_qq_name))
                .setAutoCancel(true)
                .setNumber(msgCount)
                .setSound(defaultSoundUri)
                .setDefaults(DEFAULT_LIGHTS)
                .setDeleteIntent(pendingIntentCancel);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(context.getResources().getColor(R.color.colorNotification_qq));
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            notificationBuilder.setGroup(context.getString(R.string.notification_group_qq_id));
            notificationBuilder.setGroupSummary(true);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //???????????????????????????????????????
            switch(senderType) {
                case "1":
                    notificationBuilder.setChannelId(context.getString(R.string.notification_channel_qq_contact_id));
                    break;
                case "2":
                    if(msgIsAt.equals("1")){
                        notificationBuilder.setChannelId(context.getString(R.string.notification_channel_qq_at_id));
                    } else {
                        notificationBuilder.setChannelId(context.getString(R.string.notification_channel_qq_group_id));
                    }
                    break;
                case "3":
                    if(msgIsAt.equals("1")){
                        notificationBuilder.setChannelId(context.getString(R.string.notification_channel_qq_at_id));
                    } else {
                        notificationBuilder.setChannelId(context.getString(R.string.notification_channel_qq_discuss_id));
                    }
                    break;
            }
        }


        //????????????
        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);

        //????????????
        switch (qqVibrate) {
            case "1":
                notificationBuilder.setVibrate(new long[]{0});
                break;
            case "2":
                notificationBuilder.setVibrate(new long[]{0, 100, 300, 100});
                break;
            case "3":
                notificationBuilder.setVibrate(new long[]{0, 500, 300, 500});
                break;
            default:
                notificationBuilder.setVibrate(new long[]{0});
        }

        Boolean qqIsReply=mySettings.getBoolean("check_box_preference_qq_reply",false);
        if(qqIsReply)
            //????????????7.0???????????????????????????????????????????????????
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                //?????????????????????????????????????????????
                Boolean notificationReply = mySettings.getBoolean("check_box_preference_notification_reply",true);
                if (notificationReply) {
                    Intent intentReply = new Intent(context, ReplyService.class);
                    intentReply.putExtras(msgDialogBundle);
                    PendingIntent pendingIntentReply = PendingIntent.getService(context, notifyId, intentReply, PendingIntent.FLAG_UPDATE_CURRENT);
                    String replyLabel = context.getString(R.string.notification_action_reply);
                    RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                            .setLabel(replyLabel)
                            .build();
                    NotificationCompat.Action reply_N = new NotificationCompat.Action.Builder(0, replyLabel, pendingIntentReply)
                            .addRemoteInput(remoteInput)
                            .setAllowGeneratedReplies(true)
                            .build();
                    notificationBuilder.addAction(reply_N);
                } else {
                    notificationBuilder.addAction(0, context.getString(R.string.notification_action_reply), pendingIntentDialog);
                }
            } else {
                notificationBuilder.addAction(0, context.getString(R.string.notification_action_reply), pendingIntentDialog);
            }
        notificationBuilder.addAction(0, context.getString(R.string.notification_action_list), pendingIntentList);
        notificationBuilder.addAction(0, context.getString(R.string.notification_action_clear), pendingIntentCancel);
        // notificationBuilder.addAction(0, "??????", pendingIntentPause);

        //??????????????????
        switch (qqNotifyClick) {
            case "1":
                notificationBuilder.setContentIntent(pendingIntentList);
                break;
            case "2":
                notificationBuilder.setContentIntent(pendingIntentDialog);
                break;
            case "3":
                notificationBuilder.setContentIntent(pendingIntentQq);
                break;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifyId, notificationBuilder.build());

    }

    //??????????????????
    private static void sendNotificationWx(Context context ,String msgTitle, String msgBody, int notifyId, int msgCount, String wxSound, String wxVibrate, String msgId,
                                           String senderType, String wxPackgeName, String msgIsAt)
    {
        SharedPreferences mySettings = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String wxNotifyClick = mySettings.getString("wx_notify_click", "1");

        Bundle msgNotifyBundle = new Bundle();
        msgNotifyBundle.putInt("notifyId", notifyId);
        msgNotifyBundle.putString("wxPackgeName", wxPackgeName);

        //??????????????????(?????????)
        Intent intentCancel = new Intent(context, WeixinNotificationBroadcastReceiver.class);
        intentCancel.setAction("weixin_notification_cancelled");
        intentCancel.putExtras(msgNotifyBundle);
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(context, notifyId, intentCancel, PendingIntent.FLAG_UPDATE_CURRENT);

        //???????????? ?????????????????????????????????????????????????????????
        Intent intentList = new Intent(context, MainActivity.class);
        intentList.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle msgListBundle = new Bundle();
        msgListBundle.putString("userName", msgTitle);
        msgListBundle.putString("userId", msgId);
        msgListBundle.putString("userType", WEIXIN);
        msgListBundle.putString("userMessage", msgBody);
        msgListBundle.putString("userTime", getCurTime());
        msgListBundle.putString("senderType", senderType);
        msgListBundle.putInt("notifyId", notifyId);
        msgListBundle.putString("msgCount", String.valueOf(msgCount));
        intentList.putExtras(msgListBundle);
        PendingIntent pendingIntentList = PendingIntent.getActivity(context, notifyId, intentList, PendingIntent.FLAG_UPDATE_CURRENT);

        //???????????????????????????
        Intent intentWx = new Intent(context, WeixinNotificationBroadcastReceiver.class);
        intentWx.setAction("weixin_notification_clicked");
        intentWx.putExtras(msgNotifyBundle);
        PendingIntent pendingIntentWx = PendingIntent.getBroadcast(context, notifyId, intentWx, PendingIntent.FLAG_ONE_SHOT);

        //????????????
        Intent intentDialog = new Intent(context, DialogActivity.class);

        Bundle msgDialogBundle = new Bundle();
        msgDialogBundle.putString("msgId", msgId);
        msgDialogBundle.putString("senderType", senderType);
        msgDialogBundle.putString("msgType", WEIXIN);
        msgDialogBundle.putString("msgTitle", msgTitle);
        msgDialogBundle.putString("msgBody", msgBody);
        msgDialogBundle.putInt("notifyId", notifyId);
        msgDialogBundle.putString("msgTime", getCurTime());
        msgDialogBundle.putString("wxPackgeName", wxPackgeName);
        msgDialogBundle.putString("fromNotify", "1");

        intentDialog.putExtras(msgDialogBundle);
        intentDialog.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntentDialog = PendingIntent.getActivity(context, notifyId, intentDialog, PendingIntent.FLAG_UPDATE_CURRENT);

        StringBuffer tickerWx = new StringBuffer();
        tickerWx.append(msgTitle);
        tickerWx.append("\r\n");
        tickerWx.append(msgBody);

        if (msgCount != 1)
        {
            msgTitle = msgTitle + "(" + msgCount + context.getString(R.string.notify_title_msgcount_new) +")";
        }

        Uri defaultSoundUri = Uri.parse(wxSound);

        NotificationCompat.Builder notificationBuilder = null;
        /**
         * Recommended usage for Android O:
         * NotificationCompat.Builder(Context context, String channelId);
         *
         * commented by Alex Wang
         * at 20180205
         */
        notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.weixin_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.weixin))
                .setTicker(tickerWx)
                .setContentTitle(msgTitle)
                .setStyle(new NotificationCompat.BigTextStyle() // ???????????????????????????????????????
                        .bigText(msgBody))
                .setContentText(msgBody)
                //   .setSubText(context.getString(R.string.notification_group_wechat_name))
                .setAutoCancel(true)
                .setNumber(msgCount)
                .setSound(defaultSoundUri)
                .setDefaults(DEFAULT_LIGHTS)
                .setDeleteIntent(pendingIntentCancel);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(context.getResources().getColor(R.color.colorNotification_wechat));
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            notificationBuilder.setGroup(context.getString(R.string.notification_group_wechat_id));
            notificationBuilder.setGroupSummary(true);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            switch(senderType) {
                case "1":
                    notificationBuilder.setChannelId(context.getString(R.string.notification_channel_wechat_contact_id));
                    break;
                case "2":
                    if(msgIsAt.equals("1")){
                        notificationBuilder.setChannelId(context.getString(R.string.notification_channel_wechat_at_id));
                    } else {
                        notificationBuilder.setChannelId(context.getString(R.string.notification_channel_wechat_group_id));
                    }
                    break;
                case "3":
                    if(msgIsAt.equals("1")){
                        notificationBuilder.setChannelId(context.getString(R.string.notification_channel_wechat_at_id));
                    } else {
                        notificationBuilder.setChannelId(context.getString(R.string.notification_channel_wechat_discuss_id));
                    }
                    break;
            }
        }

        //????????????
        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);

        //????????????
        switch (wxVibrate) {
            case "1":
                notificationBuilder.setVibrate(new long[]{0});
                break;
            case "2":
                notificationBuilder.setVibrate(new long[]{0, 100, 300, 100});
                break;
            case "3":
                notificationBuilder.setVibrate(new long[]{0, 500, 300, 500});
                break;
            default:
                notificationBuilder.setVibrate(new long[]{0});
        }


        Boolean wxIsReply=mySettings.getBoolean("check_box_preference_wx_reply",false);
        if(wxIsReply)
            //????????????7.0???????????????????????????????????????????????????????????????????????????
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                //?????????????????????????????????????????????
                Boolean notificationReply = mySettings.getBoolean("check_box_preference_notification_reply",true);
                if (notificationReply) {
                    Intent intentReply = new Intent(context, ReplyService.class);
                    intentReply.putExtras(msgDialogBundle);
                    PendingIntent pendingIntentReply = PendingIntent.getService(context, notifyId, intentReply, PendingIntent.FLAG_UPDATE_CURRENT);
                    String replyLabel = context.getString(R.string.notification_action_reply);
                    RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                            .setLabel(replyLabel)
                            .build();
                    NotificationCompat.Action reply_N = new NotificationCompat.Action.Builder(0, replyLabel, pendingIntentReply)
                            .addRemoteInput(remoteInput)
                            .setAllowGeneratedReplies(true)
                            .build();
                    notificationBuilder.addAction(reply_N);
                } else {
                    notificationBuilder.addAction(0, context.getString(R.string.notification_action_reply), pendingIntentDialog);
                }
            } else {
                notificationBuilder.addAction(0, context.getString(R.string.notification_action_reply), pendingIntentDialog);
            }
        notificationBuilder.addAction(0, context.getString(R.string.notification_action_list), pendingIntentList);
        notificationBuilder.addAction(0, context.getString(R.string.notification_action_clear), pendingIntentCancel);

        //??????????????????
        switch (wxNotifyClick) {
            case "1":
                notificationBuilder.setContentIntent(pendingIntentList);
                break;
            case "2":
                notificationBuilder.setContentIntent(pendingIntentDialog);
                break;
            case "3":
                notificationBuilder.setContentIntent(pendingIntentWx);
                break;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifyId, notificationBuilder.build());
    }


    //??????????????????
    private static void sendNotificationSys(Context context, String msgTitle, String msgBody, String msgId, int notifyId, int msgCount)
    {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle msgListBundle = new Bundle();
        msgListBundle.putString("userName", msgTitle);
        msgListBundle.putString("userId", msgId);
        msgListBundle.putString("userType", SYS);
        msgListBundle.putString("userMessage", msgBody);
        msgListBundle.putString("userTime", getCurTime());
        msgListBundle.putString("senderType", "1");
        msgListBundle.putInt("notifyId", notifyId);
        msgListBundle.putString("msgCount", String.valueOf(msgCount));
        intent.putExtras(msgListBundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notifyId /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Bundle msgNotifyBundle = new Bundle();
        msgNotifyBundle.putInt("notifyId", notifyId);

        //??????????????????
        Intent intentCancel = new Intent(context, SysNotificationBroadcastReceiver.class);
        intentCancel.setAction("sys_notification_cancelled");
        intentCancel.putExtras(msgNotifyBundle);
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(context, notifyId, intentCancel, PendingIntent.FLAG_ONE_SHOT);

        StringBuffer tickerSys = new StringBuffer();
        tickerSys.append(msgTitle);
        tickerSys.append("\r\n");
        tickerSys.append(msgBody);

        if (msgCount != 1)
        {
            msgTitle = msgTitle + "(" + msgCount +context.getString(R.string.notify_title_msgcount_new) +")";
        }

        int smallIcon;
        Bitmap largeIcon;

        switch (msgId) {
            case "1":
                smallIcon = R.drawable.qq_notification;
                largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.qq);
                break;
            case "2":
                smallIcon = R.drawable.weixin_notification;
                largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.weixin);
                break;
            default:
                smallIcon = R.drawable.sys_notification;
                largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.sys);
        }

        int defaults = 0;
        defaults |= Notification.DEFAULT_LIGHTS;
        defaults |= Notification.DEFAULT_VIBRATE;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(smallIcon)
                .setLargeIcon(largeIcon)
                .setTicker(tickerSys)
                .setContentTitle(msgTitle)
                .setStyle(new NotificationCompat.BigTextStyle() // ???????????????????????????????????????
                        .bigText(msgBody))
                .setContentText(msgBody)
                //    .setSubText(context.getString(R.string.notification_group_sys_name))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setDefaults(defaults)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(pendingIntentCancel);
        notificationBuilder.setPriority(Notification.PRIORITY_HIGH); //??????????????????
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(context.getResources().getColor(R.color.colorPrimary));
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(context.getString(R.string.notification_channel_sys_id));
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifyId, notificationBuilder.build());
    }

    /**
     * ??????????????????????????????
     *
     * @param context  ??????context
     * @param serviceClassName ????????????????????????
     * @return boolean
     */
    private static boolean isServiceRunning(Context context, String serviceClassName)
    {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : services)
        {
            if (runningServiceInfo.service.getPackageName().equals(serviceClassName))
            {
                return true;
            }
        }
        return false;
    }

    //???????????????
    private static void download(Context context, String docUrl) throws Exception
    {
        //?????????????????????????????????????????????????????????
        //SD???????????????????????????????????????????????????SD????????????????????????
        String dirName = Environment.getExternalStorageDirectory() + "/GcmForMojo/";
        File f = new File(dirName);
        if (!f.exists())
        {      //???????????????????????????
            //noinspection ResultOfMethodCallIgnored
            f.mkdir();        //????????????????????????????????????????????????
        }
        //???????????????????????????
        String[] list = docUrl.split("/");
        String fileName = list[list.length - 1];
        String fileNameTemp = fileName;
        fileName = dirName + fileName;
        File file = new File(fileName);
        if (file.exists())
        {    //??????????????????????????????
            //noinspection ResultOfMethodCallIgnored
            file.delete();    //??????????????????
        }
        //1K???????????????
        byte[] bs = new byte[1024];
        //????????????????????????
        int len;
        try
        {
            //????????????????????????url??????
            URL url = new URL(docUrl);
            //????????????
            //URLConnection conn = url.openConnection();
            //???????????????
            InputStream is = url.openStream();
            //?????????????????????
            //int contextLength = conn.getContentLength();
            //??????????????????
            OutputStream os = new FileOutputStream(file);
            //????????????
            while ((len = is.read(bs)) != -1)
            {
                os.write(bs, 0, len);
            }
            //????????????????????????
            os.close();
            is.close();
        } catch (MalformedURLException e)
        {
            //fileName = null;
            System.out.println("??????URL????????????");
            throw e;
        } catch (FileNotFoundException e)
        {
            // fileName = null;
            System.out.println("??????????????????");
            throw e;
        } catch (IOException e)
        {
            //  fileName = null;
            System.out.println("??????????????????");
            throw e;
        }


        // ????????????????????????????????????
        try
        {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileNameTemp, null);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        // ????????????????????????
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(fileName));
        intent.setData(uri);
        context.sendBroadcast(intent);

        // return fileName;
    }


    /**
     * ????????????UsageStatsManager?????????????????????android5.0A???????????????API
     * ?????????
     * 1. ???????????????android5.0????????????
     * 2. AndroidManifest??????????????????<uses-permission xmlns:tools="http://schemas.android.com/tools" android:name="android.permission.PACKAGE_USAGE_STATS"
     * tools:ignore="ProtectedPermissions" />
     * 3. ?????????????????????????????????-????????????????????????????????????????????????????????????App?????????
     *
     * @param context     ???????????????
     * @param packageName ?????????????????????????????????App?????????
     */

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean queryAppUsageStats(Context context, String packageName)
    {
        class RecentUseComparator implements Comparator<UsageStats>
        {
            @Override
            public int compare(UsageStats lhs, UsageStats rhs)
            {
                return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
            }
        }

        RecentUseComparator mRecentComp = new RecentUseComparator();
        UsageStatsManager mUsageStatsManager = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
        {
            mUsageStatsManager = (UsageStatsManager) context.getSystemService(USAGE_STATS_SERVICE);
        }

        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.YEAR, -1);
        long startTime = calendar.getTimeInMillis();

        assert mUsageStatsManager != null;
        List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        if (usageStats == null || usageStats.size() == 0)
        {
            if (!HavaPermissionForTest(context))
            {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                //   Looper.prepare();
                //   Toast.makeText(context, "????????????\n????????????????????????????????????-????????????????????????????????????????????????????????????App?????????", Toast.LENGTH_SHORT).show();
                //   Looper.loop();
            }
            return false;
        }
        Collections.sort(usageStats, mRecentComp);
        String currentTopPackage = usageStats.get(0).getPackageName();
        Log.d(MYTAG, currentTopPackage);

        return currentTopPackage.equals(packageName);
    }

    /**
     * ????????????????????????
     *
     * @param context ???????????????
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean HavaPermissionForTest(Context context)
    {
        try
        {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e)
        {
            return true;
        }
    }


    /*
     *??????????????????????????????
     *
     */
    private static class MsgThread extends Thread
    {
        @Override
        public void run()
        {
            Message msg = new Message();
            msg.obj = "UpdateMsgList";
            DialogActivity.msgHandler.sendMessage(msg);
            super.run();
        }
    }

    /*
     *?????????????????????????????????
     *
     */
    private static class userThread extends Thread
    {
        @Override
        public void run()
        {
            Message msg = new Message();
            msg.obj = "UpdateCurrentUserList";
            MainActivity.userHandler.sendMessage(msg);
            super.run();
        }
    }



}
