package com.message_receiverAL.mm;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

public class MobileButlerUtil {

    private static Context mContext;

    public MobileButlerUtil(Context context){
        this.mContext = context;
    }

    public boolean isHuawei() {
        if (Build.BRAND == null) {
            return false;
        } else {
            return Build.BRAND.toLowerCase().equals("huawei") || Build.BRAND.toLowerCase().equals("honor");
        }
    }

    public static boolean isXiaomi() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("xiaomi");
    }

    public boolean isOPPO() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("oppo");
    }

    public boolean isVIVO() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("vivo");
    }

    public boolean isMeizu() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("meizu");
    }

    public boolean isSamsung() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("samsung");
    }

    public boolean isLeTV() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("letv");
    }

    public boolean isSmartisan() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("smartisan");
    }


    public void goHuaweiSetting() {
        try {
            showActivity("com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
        } catch (Exception e) {
            showActivity("com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.bootstart.BootStartActivity");
        }
    }

    public static void goXiaomiSetting() {
        showActivity("com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity");
    }

    public void goOPPOSetting() {
        try {
            showActivity("com.coloros.phonemanager");
        } catch (Exception e1) {
            try {
                showActivity("com.oppo.safe");
            } catch (Exception e2) {
                try {
                    showActivity("com.coloros.oppoguardelf");
                } catch (Exception e3) {
                    showActivity("com.coloros.safecenter");
                }
            }
        }
    }

    public void goVIVOSetting() {
        showActivity("com.iqoo.secure");
    }

    public void goMeizuSetting() {
        showActivity("com.meizu.safe");
    }

    public void goSamsungSetting() {
        try {
            showActivity("com.samsung.android.sm_cn");
        } catch (Exception e) {
            showActivity("com.samsung.android.sm");
        }
    }

    public void goLetvSetting() {
        showActivity("com.letv.android.letvsafe",
                "com.letv.android.letvsafe.AutobootManageActivity");
    }

    public void goSmartisanSetting() {
        showActivity("com.smartisanos.security");
    }

    /**
     * 跳转到指定应用的首页
     */
    public void showActivity(@NonNull String packageName) {
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        mContext.startActivity(intent);
    }

    /**
     * 跳转到指定应用的指定页面
     */
    public static void showActivity(@NonNull String packageName, @NonNull String activityDir) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityDir));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
    //已失效
//    public static boolean isAllowAutoStart(Context context,String packageName){
//        try {
//            @SuppressLint("PrivateApi")
//            Method method = Class.forName("android.miui.AppOpsUtils")
//                    .getMethod("getApplicationAutoStart", Context.class, String.class);
//            Log.d("ziqidong:",(String)method.invoke(null,context,packageName));
//            return (int) method.invoke(null,context,packageName) == 0; //0已允许, 1已拒绝
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        //如果系统更新改了api可能导致没法判断
//        return true;
//    }



}