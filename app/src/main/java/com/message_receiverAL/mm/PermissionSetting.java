package com.message_receiverAL.mm;
//
//import android.content.Context;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Build;
//import android.os.PowerManager;
//import android.provider.Settings;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.annotation.Nullable;
//import androidx.annotation.RequiresApi;
//import androidx.fragment.app.Fragment;
//
//import static androidx.core.app.ActivityCompat.startActivityForResult;
//
//public class PermissionSetting<BaseFragment> extends BaseFragment implements View.OnClickListener {
//
//
//    @BindView(R.id.battery_white)
//    TextView batteryWhite;
//
//    @BindView(R.id.operation_permission)
//    TextView operationPermission;
//
//    @BindView(R.id.iv_goback)
//    TextView goBack;
//
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.battery_white:
//                //加入电池白名单
//                requestIgnoreBatteryOptimizations();
//                break;
//            case R.id.operation_permission:
//                checkPermission();//设置允许后台活动
//                break;
//            case R.id.iv_goback:
//                pop();
//                break;
//        }
//
//    }
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
//
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    public void requestIgnoreBatteryOptimizations() {
//        try {
//            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
//            startActivityForResult(intent,1);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 1) {
//            if(isIgnoringBatteryOptimizations()){
//                batteryWhite.setText("已开启");
//                batteryWhite.setBackground(null);
//                batteryWhite.setEnabled(false);
//            }
//        }
//    }
//
//    public void checkPermission(){
//        MobileButlerUtil mobileButlerUtil = new MobileButlerUtil(getContext());
//        final PromptPopup promptPopup = new PromptPopup(getContext())
//                .setContent("为了接收到账通知，请去设置易掌管app允许在后台活动")
//                .withConfirmClick(v -> {
//                    if(mobileButlerUtil.isHuawei()){
//                        mobileButlerUtil.goHuaweiSetting();
//                    }else if(mobileButlerUtil.isXiaomi()){
//                        mobileButlerUtil.goXiaomiSetting();
//                    }else if(mobileButlerUtil.isOPPO()){
//                        mobileButlerUtil.goOPPOSetting();
//                    }else if(mobileButlerUtil.isVIVO()){
//                        mobileButlerUtil.goVIVOSetting();
//                    }else if(mobileButlerUtil.isMeizu()){
//                        mobileButlerUtil.goMeizuSetting();
//                    }else if(mobileButlerUtil.isSamsung()){
//                        mobileButlerUtil.goSamsungSetting();
//                    }else if(mobileButlerUtil.isLeTV()){
//                        mobileButlerUtil.goLetvSetting();
//                    }else if(mobileButlerUtil.isSmartisan()){
//                        mobileButlerUtil.goSmartisanSetting();
//                    }
//                }, true);
//        promptPopup.showPopupWindow();
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    @Override
//    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
//        super.onLazyInitView(savedInstanceState);
//        ImmersionBar.with(this).titleBar(R.id.ly_title_bar).autoDarkModeEnable(true).statusBarColor(R.color.bar_grey).init();
//        batteryWhite.setOnClickListener(this);
//        operationPermission.setOnClickListener(this);
//        goBack.setOnClickListener(this);
//
//        if(isIgnoringBatteryOptimizations()){
//            batteryWhite.setText("已开启");
//            batteryWhite.setBackground(null);
//            batteryWhite.setEnabled(false);
//        }
//    }
//
//
//
//    @Override
//    protected int getLayoutId() {
//        return R.layout.permission_setting;
//    }
//}