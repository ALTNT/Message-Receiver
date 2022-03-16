package com.message_receiverAL.mm;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by HeiPi on 2017/1/24.
 * 处理QQ通知点击
 */

public class QqNotificationBroadcastReceiver extends BroadcastReceiver {

   // private MyApplication MyApplication;


    @Override
    public void onReceive(Context context, Intent intent) {

        ArrayList<User> currentUserList;
        Map<Integer, Integer> msgCountMap;

      //  MyApplication = (MyApplication) context.getApplicationContext();
        currentUserList = DemoApplication.getInstance().getCurrentUserList();
        msgCountMap = DemoApplication.getInstance().getMsgCountMap();

        String action = intent.getAction();
        Bundle msgNotifyBundle = intent.getExtras();
        Integer notifyId = msgNotifyBundle.getInt("notifyId");
        String qqPackgeName=msgNotifyBundle.getString("qqPackgeName");

        if (notifyId != -1) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notifyId);
            for(int    i=0;    i<currentUserList.size();    i++){
                if(currentUserList.get(i).getNotifyId()==notifyId){
                    currentUserList.get(i).setMsgCount("0");
                    if(MainActivity.userHandler!=null)
                        new userThread().start();
                    break;
                }
            }
        }

        if (action.equals("qq_notification_clicked")) {
            //处理点击事件

                // 通过包名获取要跳转的app，创建intent对象
                Intent intentNewQq = context.getPackageManager().getLaunchIntentForPackage(qqPackgeName);

                if (intentNewQq != null) {
                    if (msgCountMap.get(notifyId) != null)
                        msgCountMap.put(notifyId, 0);
                    context.startActivity(intentNewQq);
                } else {
                    // 没有安装要跳转的app应用进行提醒
                    Toast.makeText(context.getApplicationContext(), R.string.toast_check_package_fail + qqPackgeName, Toast.LENGTH_LONG).show();
                }

        }

        if (action.equals("qq_notification_cancelled")) {
            //处理滑动清除和点击删除事件
            if(msgCountMap.get(notifyId)!=null)
            msgCountMap.put(notifyId,0);
        }

    }

    /*
*子线程处理会话界面通信
*
*/
    private class userThread extends Thread {
        @Override
        public void run() {
            Message msg = new Message();
            msg.obj = "UpdateCurrentUserList";
            MainActivity.userHandler.sendMessage(msg);
            super.run();
        }
    }
}
