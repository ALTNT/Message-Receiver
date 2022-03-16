package com.message_receiverAL.mm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.List;
import java.util.Map;

public class SettingActivity extends AppCompatActivity {
    private String alias=null;
    // user your appid the key.
    private static final String APP_ID = "";
    // user your appid the key.
    private static final String APP_KEY = "";

    private ImageView ivBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);
        // 设置别名

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.set_alias).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(SettingActivity.this);
                new AlertDialog.Builder(SettingActivity.this)//用来显示要输入的文本框
                        .setTitle(R.string.set_alias)
                        .setView(editText)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alias = editText.getText().toString();
                                MiPushClient.setAlias(SettingActivity.this, alias, null);
                                try{
                                    MiPushClient.unsetAlias(SettingActivity.this,(String)ObjectSaveUtils.getObject(SettingActivity.this,"savedalias"),null);
                                    ObjectSaveUtils.saveObject(SettingActivity.this,"savedalias",alias);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }

                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
        // 撤销别名
        findViewById(R.id.unset_alias).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(SettingActivity.this);
                new AlertDialog.Builder(SettingActivity.this)
                        .setTitle(R.string.unset_alias)
                        .setView(editText)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String alias = editText.getText().toString();
                                MiPushClient.unsetAlias(SettingActivity.this, alias, null);
                            }

                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();

            }
        });
        // 设置帐号
        findViewById(R.id.set_account).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(SettingActivity.this);
                new AlertDialog.Builder(SettingActivity.this)
                        .setTitle(R.string.set_account)
                        .setView(editText)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String account = editText.getText().toString();
                                MiPushClient.setUserAccount(SettingActivity.this, account, null);
                            }

                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();

            }
        });
        // 撤销帐号
        findViewById(R.id.unset_account).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(SettingActivity.this);
                new AlertDialog.Builder(SettingActivity.this)
                        .setTitle(R.string.unset_account)
                        .setView(editText)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String account = editText.getText().toString();
                                MiPushClient.unsetUserAccount(SettingActivity.this, account, null);
                            }

                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
        // 设置标签
        findViewById(R.id.subscribe_topic).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(SettingActivity.this);
                new AlertDialog.Builder(SettingActivity.this)
                        .setTitle(R.string.subscribe_topic)
                        .setView(editText)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String topic = editText.getText().toString();
                                MiPushClient.subscribe(SettingActivity.this, topic, null);
                            }

                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
        // 撤销标签
        findViewById(R.id.unsubscribe_topic).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(SettingActivity.this);
                new AlertDialog.Builder(SettingActivity.this)
                        .setTitle(R.string.unsubscribe_topic)
                        .setView(editText)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String topic = editText.getText().toString();
                                MiPushClient.unsubscribe(SettingActivity.this, topic, null);
                            }

                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
        // 设置接收消息时间
        findViewById(R.id.set_accept_time).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new TimeIntervalDialog(SettingActivity.this, new TimeIntervalDialog.TimeIntervalInterface() {

                    @Override
                    public void apply(int startHour, int startMin, int endHour,
                                      int endMin) {
                        MiPushClient.setAcceptTime(SettingActivity.this, startHour, startMin, endHour, endMin, null);
                    }

                    @Override
                    public void cancel() {
                        //ignore
                    }

                })
                        .show();
            }
        });
        // 暂停推送
        findViewById(R.id.pause_push).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MiPushClient.pausePush(SettingActivity.this, null);
            }
        });

        findViewById(R.id.resume_push).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MiPushClient.resumePush(SettingActivity.this, null);
            }
        });

        //设置登录校园网
        findViewById(R.id.login_wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                loginBjtuWifi();
            }
        });
        findViewById(R.id.set_self_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MobileButlerUtil(SettingActivity.this).goXiaomiSetting();
            }
        });
        findViewById(R.id.chat_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    @Override
    protected void onResume() {
//        onStart()通常就是onStop()(用户按下home键，activity变为后台)之后用户再切回这个activity就会调用onRestart()然后调用onStart();
//        onResume()是onPaused()（activity被另一个透明或者Dialog样式的activity覆盖了）之后dialog取消，activity回到可交互状态，调用onResume();
        super.onResume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void saveMsg(){
        Map<String, List<Spanned>> msgSave = DemoApplication.getInstance().getMsgSave();
        try{
            ObjectSaveUtils.saveObject(SettingActivity.this,"key",msgSave);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}