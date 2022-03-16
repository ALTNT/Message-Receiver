package com.message_receiverAL.mm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.message_receiverAL.mm.DemoApplication.QQ;
import static com.message_receiverAL.mm.DemoApplication.WEIXIN;
import static com.xiaomi.mipush.sdk.MiPushClient.clearNotification;

/**
 * 1、PushMessageReceiver 是个抽象类，该类继承了 BroadcastReceiver。<br/>
 * 2、需要将自定义的 DemoMessageReceiver 注册在 AndroidManifest.xml 文件中：
 * <pre>
 * {@code
 *  <receiver
 *      android:name="com.xiaomi.mipushdemo.DemoMessageReceiver"
 *      android:exported="true">
 *      <intent-filter>
 *          <action android:name="com.xiaomi.mipush.RECEIVE_MESSAGE" />
 *      </intent-filter>
 *      <intent-filter>
 *          <action android:name="com.xiaomi.mipush.MESSAGE_ARRIVED" />
 *      </intent-filter>
 *      <intent-filter>
 *          <action android:name="com.xiaomi.mipush.ERROR" />
 *      </intent-filter>
 *  </receiver>
 *  }</pre>
 * 3、DemoMessageReceiver 的 onReceivePassThroughMessage 方法用来接收服务器向客户端发送的透传消息。<br/>
 * 4、DemoMessageReceiver 的 onNotificationMessageClicked 方法用来接收服务器向客户端发送的通知消息，
 * 这个回调方法会在用户手动点击通知后触发。<br/>
 * 5、DemoMessageReceiver 的 onNotificationMessageArrived 方法用来接收服务器向客户端发送的通知消息，
 * 这个回调方法是在通知消息到达客户端时触发。另外应用在前台时不弹出通知的通知消息到达客户端也会触发这个回调函数。<br/>
 * 6、DemoMessageReceiver 的 onCommandResult 方法用来接收客户端向服务器发送命令后的响应结果。<br/>
 * 7、DemoMessageReceiver 的 onReceiveRegisterResult 方法用来接收客户端向服务器发送注册命令后的响应结果。<br/>
 * 8、以上这些方法运行在非 UI 线程中。
 *
 * @author mayixiang
 */
public class DemoMessageReceiver extends PushMessageReceiver {
    private String mRegId;
    private String mTopic;
    private String mAlias;
    private String mAccount;
    private String mStartTime;
    private String mEndTime;
    public String arrayToString(String[] paramArrayOfString) {
        int j = paramArrayOfString.length;
        int i = 0;
        String str = " ";
        while (i < j) {
            String str1 = paramArrayOfString[i];
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str);
            stringBuilder.append(str1);
            stringBuilder.append(" ");
            str = stringBuilder.toString();
            i++;
        }
        return str;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {
//        onReceivePassThroughMessage 方法用来接收服务器向客户端发送的透传消息
        System.out.println(message.toString());
        Log.v(DemoApplication.TAG,
                "onReceivePassThroughMessage is called. " + message.toString());
        String log = context.getString(R.string.recv_passthrough_message, message.getContent());
        MainActivity.logList.add(0, getSimpleDate() + " " + log);

        if (!TextUtils.isEmpty(message.getTopic())) {
            mTopic = message.getTopic();
        } else if (!TextUtils.isEmpty(message.getAlias())) {
            mAlias = message.getAlias();
        }

        Message msg = Message.obtain();
        msg.obj = log;
        DemoApplication.getHandler().sendMessage(msg);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {
//        onNotificationMessageClicked 方法用来接收服务器向客户端发送的通知消息，
//        这个回调方法会在用户手动点击通知后触发。
//        DemoApplication.makeToast();
        System.out.println(message.toString());
        Log.v(DemoApplication.TAG,
                "onNotificationMessageClicked is called. " + message.toString());
        String log = context.getString(R.string.click_notification_message, message.getContent());

        if (!TextUtils.isEmpty(message.getTopic())) {
            mTopic = message.getTopic();
        } else if (!TextUtils.isEmpty(message.getAlias())) {
            mAlias = message.getAlias();
        }

        Message msg = Message.obtain();
        if (message.isNotified()) {
            msg.obj = "@%^&"+message.getTitle();
        }


//        DemoApplication.openapp(message);
        DemoApplication.getHandler().sendMessage(msg);
        if(message.getTitle().contains("QQ")){
            for(int i=DemoApplication.notifyIdsQQ.size()-1;i>=0;i--){
                clearNotification(DemoApplication.getHandler().getContext(),DemoApplication.notifyIdsQQ.get(i));
                DemoApplication.notifyIdsQQ.remove(i);
            }
        }
        else if(message.getTitle().contains("微信")){
            for(int i=DemoApplication.notifyIdswechat.size()-1;i>=0;i--){
                clearNotification(DemoApplication.getHandler().getContext(),DemoApplication.notifyIdswechat.get(i));
                DemoApplication.notifyIdswechat.remove(i);
            }
        }
        else {
            for(int i=DemoApplication.notifyIdsOthers.size()-1;i>=0;i--){
                clearNotification(DemoApplication.getHandler().getContext(),DemoApplication.notifyIdsOthers.get(i));
                DemoApplication.notifyIdsOthers.remove(i);
            }
        }
        MainActivity.logList.add(0, getSimpleDate() + " " + log);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {
//        onNotificationMessageArrived 方法用来接收服务器向客户端发送的通知消息，
// * 这个回调方法是在通知消息到达客户端时触发。另外应用在前台时不弹出通知的通知消息到达客户端也会触发这个回调函数。<br/>
        String ss= null;
        try{
            ss = (String)ObjectSaveUtils.getObject(DemoApplication.getInstance(),"savedalias");
        }catch (Exception e){
            e.printStackTrace();
        }
        if( message.getAlias().equals(ss)){
            try{
                JSONObject remoteMessage = new JSONObject();
                if(message.getTitle().contains("QQ")||message.getTitle().contains("TIM")){
                    DemoApplication.notifyIdsQQ.add(message.getNotifyId());
                    if(message.getTitle().contains("QQ群聊")){
                        remoteMessage.put("senderType","2");//2表示群聊
                        remoteMessage.put("msgId",message.getTitle().substring(message.getTitle().split("聊")[0].length()+2));
                        remoteMessage.put("type",QQ);
                        remoteMessage.put("title",message.getTitle());
                        remoteMessage.put("message",message.getContent());
                        remoteMessage.put("isAt","");
                        MessageUtil.MessageUtilDo(context,remoteMessage.getString("msgId"),remoteMessage.getString("type"),remoteMessage.getString("senderType"),remoteMessage.getString("title"),remoteMessage.getString("message"),null);
                    }else{
                        if(message.getTitle().startsWith("来自QQ")||message.getTitle().startsWith("来自TIM")){//QQ或Tim转发的消息
                            if(message.getContent().split(":")[0].contains("(")&&message.getContent().split(":")[0].contains(")")){//QQ或Tim转发的群聊消息
                                remoteMessage.put("senderType","1");//1表示消息
                                remoteMessage.put("msgId",message.getContent().split(":")[0]);
                                remoteMessage.put("type",QQ);
                                remoteMessage.put("title","QQ群聊".concat(" ").concat(message.getContent().substring(message.getContent().indexOf('(')+1,message.getContent().indexOf(')'))));
                                remoteMessage.put("message",message.getContent());
                                remoteMessage.put("isAt","");
                                MessageUtil.MessageUtilDo(context,remoteMessage.getString("msgId"),remoteMessage.getString("type"),remoteMessage.getString("senderType"),remoteMessage.getString("title"),remoteMessage.getString("message"),null);
                            }
                            else{//QQ或Tim转发的普通消息
                                remoteMessage.put("senderType","1");//1表示消息
                                remoteMessage.put("msgId",message.getContent().split(":")[0]);
                                remoteMessage.put("type",QQ);
                                remoteMessage.put("title","QQ".concat(" ").concat(message.getContent().split(":")[0]));
                                remoteMessage.put("message",message.getContent().substring(message.getContent().split(":")[0].length()+1));
                                remoteMessage.put("isAt","");
                                MessageUtil.MessageUtilDo(context,remoteMessage.getString("msgId"),remoteMessage.getString("type"),remoteMessage.getString("senderType"),remoteMessage.getString("title"),remoteMessage.getString("message"),null);
                            }
                        }else{
                            //QQ脚本发过来的消息
                            remoteMessage.put("senderType","1");//1表示消息
                            remoteMessage.put("msgId",message.getContent().split(":")[0]);
                            remoteMessage.put("type",QQ);
                            remoteMessage.put("title",message.getTitle().concat(" ").concat(message.getContent().split(":")[0]));
                            remoteMessage.put("message",message.getContent().substring(message.getContent().split(":")[0].length()+1));
                            remoteMessage.put("isAt","");
                            MessageUtil.MessageUtilDo(context,remoteMessage.getString("msgId"),remoteMessage.getString("type"),remoteMessage.getString("senderType"),remoteMessage.getString("title"),remoteMessage.getString("message"),null);
                        }

                    }
                }
                else if(message.getTitle().contains("微信")||message.getTitle().contains("WeChat")){
                    DemoApplication.notifyIdswechat.add(message.getNotifyId());
                    if(message.getTitle().contains("微信群聊")){
                        remoteMessage.put("senderType","2");//4表示群聊
                        remoteMessage.put("msgId",message.getTitle().substring(message.getTitle().split("聊")[0].length()+2));
                        remoteMessage.put("type",WEIXIN);
                        remoteMessage.put("title",message.getTitle());
                        remoteMessage.put("message",message.getContent());
                        remoteMessage.put("isAt","");
                        MessageUtil.MessageUtilDo(context,remoteMessage.getString("msgId"),remoteMessage.getString("type"),remoteMessage.getString("senderType"),remoteMessage.getString("title"),remoteMessage.getString("message"),null);
                    }else{
                        remoteMessage.put("senderType","1");//1表示微信消息
                        remoteMessage.put("msgId",message.getContent().split(":")[0]);
                        remoteMessage.put("type",WEIXIN);
                        remoteMessage.put("title",message.getTitle().concat(" ").concat(message.getContent().split(":")[0]));
                        remoteMessage.put("message",message.getContent().substring(message.getContent().split(":")[0].length()+1));
                        remoteMessage.put("isAt","");
                        MessageUtil.MessageUtilDo(context,remoteMessage.getString("msgId"),remoteMessage.getString("type"),remoteMessage.getString("senderType"),remoteMessage.getString("title"),remoteMessage.getString("message"),null);
                    }
                }
                else
                    DemoApplication.notifyIdsOthers.add(message.getNotifyId());
                Log.v(DemoApplication.TAG,
                        "onNotificationMessageArrived is called. " + message.toString());
                String msgString=message.toString();

                String log = context.getString(R.string.arrive_notification_message, message.getContent());

                MainActivity.logList.add(0, getSimpleDate() + " " + log);

                if (!TextUtils.isEmpty(message.getTopic())) {
                    mTopic = message.getTopic();
                } else if (!TextUtils.isEmpty(message.getAlias())) {
                    mAlias = message.getAlias();
                }

                Message msg = Message.obtain();
//        msg.obj = log;
                DemoApplication.getHandler().sendMessage(msg);
            }catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

    }

    @SuppressLint("LongLogTag")
    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
//        onCommandResult 方法用来接收客户端向服务器发送命令后的响应结果。
        System.out.println(message.toString());
        Log.v(DemoApplication.TAG,
                "onCommandResult is called. " + message.toString());
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        String log;
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
                log = context.getString(R.string.register_success);
            } else {
                log = context.getString(R.string.register_fail);
            }
        } else if (MiPushClient.COMMAND_SET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
                log = context.getString(R.string.set_alias_success, mAlias);
            } else {
                log = context.getString(R.string.set_alias_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_UNSET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
                log = context.getString(R.string.unset_alias_success, mAlias);
            } else {
                log = context.getString(R.string.unset_alias_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_SET_ACCOUNT.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAccount = cmdArg1;
                log = context.getString(R.string.set_account_success, mAccount);
            } else {
                log = context.getString(R.string.set_account_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_UNSET_ACCOUNT.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAccount = cmdArg1;
                log = context.getString(R.string.unset_account_success, mAccount);
            } else {
                log = context.getString(R.string.unset_account_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
                log = context.getString(R.string.subscribe_topic_success, mTopic);
            } else {
                log = context.getString(R.string.subscribe_topic_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
                log = context.getString(R.string.unsubscribe_topic_success, mTopic);
            } else {
                log = context.getString(R.string.unsubscribe_topic_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_SET_ACCEPT_TIME.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mStartTime = cmdArg1;
                mEndTime = cmdArg2;
                log = context.getString(R.string.set_accept_time_success, mStartTime, mEndTime);
            } else {
                log = context.getString(R.string.set_accept_time_fail, message.getReason());
            }
        } else {
            log = message.getReason();
        }
        MainActivity.logList.add(0, getSimpleDate() + "    " + log);

        Message msg = Message.obtain();
        msg.obj = log;
        DemoApplication.getHandler().sendMessage(msg);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
//        onReceiveRegisterResult 方法用来接收客户端向服务器发送注册命令后的响应结果。
        System.out.println(message.toString());
        Log.v(DemoApplication.TAG,
                "onReceiveRegisterResult is called. " + message.toString());
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String log;
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
                log = context.getString(R.string.register_success);
            } else {
                log = context.getString(R.string.register_fail);
            }
        } else {
            log = message.getReason();
        }

        Message msg = Message.obtain();
        msg.obj = log;
        DemoApplication.getHandler().sendMessage(msg);
    }

    @SuppressLint("SimpleDateFormat")
    private static String getSimpleDate() {
        return new SimpleDateFormat("MM-dd hh:mm:ss").format(new Date());
    }

//    @SuppressLint("LongLogTag")
//    public void onRequirePermissions(Context paramContext, String[] paramArrayOfString) {
//        super.onRequirePermissions(paramContext, paramArrayOfString);
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("onRequirePermissions is called. need permission");
//        stringBuilder.append(arrayToString(paramArrayOfString));
//        Log.e("com.message_receiverAL.mm", stringBuilder.toString());
//        if (Build.VERSION.SDK_INT >= 23 && (paramContext.getApplicationInfo()).targetSdkVersion >= 23) {
//            Intent intent = new Intent();
//            intent.putExtra("permissions", paramArrayOfString);
//            intent.setComponent(new ComponentName(paramContext.getPackageName(), PermissionActivity.class.getCanonicalName()));
//            intent.addFlags(276824064);
//            paramContext.startActivity(intent);
//        }
//    }

}
