package com.message_receiverAL.mm;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by HeiPi on 2017/1/15.
 * 默认处理通知事件
 */

public class SysNotificationBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        ArrayList<User> currentUserList;
        Map<Integer, Integer> msgCountMap;

        currentUserList = DemoApplication.getInstance().getCurrentUserList();
        msgCountMap = DemoApplication.getInstance().getMsgCountMap();

        String action = intent.getAction();
        Bundle msgNotifyBundle = intent.getExtras();
        Integer notifyId = msgNotifyBundle.getInt("notifyId");

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

        if (action.equals("sys_notification_clicked")) {
            //处理点击事件

            msgCountMap.put(notifyId,0);
        }

        if (action.equals("sys_notification_cancelled")) {
            //处理滑动清除和点击删除事件

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
