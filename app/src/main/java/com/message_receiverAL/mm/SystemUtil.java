package com.message_receiverAL.mm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;

//import static androidx.core.app.ActivityCompat.startActivityForResult;

public class SystemUtil {
    private Context context=null;
    /**
     * 是否在白名单内
     * @param context
     * @return
     */
    @SuppressLint("LongLogTag")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isSystemWhiteList(Context context){
        context=context;
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        String packageName = context.getPackageName();
        boolean isWhite = pm.isIgnoringBatteryOptimizations(packageName);
        Log.e("SystemUtil","SystemUtil.isSystemWhiteList.packageName="+packageName+",isWhite="+isWhite);
        return isWhite;
    }

    private Context getContext() {
        return context;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getContext().getPackageName());
        }
        return isIgnoring;
    }
}
