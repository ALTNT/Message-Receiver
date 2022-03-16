package com.message_receiverAL.mm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String action_boot="android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(
            Context context, Intent intent) {
        if (intent.getAction().equals(action_boot)){
            Intent ootStartIntent=new Intent(context,MainActivity.class);
            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(ootStartIntent);
        }else {
            //开启前台服务
            Intent intent_service = new Intent(MainActivity.getInstance(), WhiteService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {//8.0以上的开启方式不同
                context.startForegroundService(intent_service);
            } else {
                context.startService(intent_service);
            }
        }
    }
}