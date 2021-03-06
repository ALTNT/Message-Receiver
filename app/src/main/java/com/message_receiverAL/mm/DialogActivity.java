package com.message_receiverAL.mm;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.message_receiverAL.mm.MessageUtil.MessageUtilDo;
import static com.message_receiverAL.mm.DemoApplication.PREF;
import static com.message_receiverAL.mm.DemoApplication.QQ;
import static com.message_receiverAL.mm.DemoApplication.SYS;
import static com.message_receiverAL.mm.DemoApplication.WEIXIN;
import static com.message_receiverAL.mm.DemoApplication.getColorMsgTime;
import static com.message_receiverAL.mm.DemoApplication.getCurTime;
import static com.message_receiverAL.mm.DemoApplication.isQqOnline;
import static com.message_receiverAL.mm.DemoApplication.isWxOnline;
import static com.message_receiverAL.mm.DemoApplication.mySettings;
import static com.message_receiverAL.mm.DemoApplication.qqColor;
import static com.message_receiverAL.mm.DemoApplication.sysReceiveColor;
import static com.message_receiverAL.mm.DemoApplication.toSpannedMessage;
import static com.message_receiverAL.mm.DemoApplication.wxColor;


public class DialogActivity extends Activity implements View.OnClickListener {

    private ArrayList<User> currentUserList;
    private Map<String, List<Spanned>> msgSave;
    SQLiteOpenHelper openHelper;
    private Map<Integer, Integer> msgCountMap;

    private View line_view;
    private EditText editText_content;
    private ListView msgListView;
    private String msgId;
    private String msgTitle;
    private String msgBody;
    private String senderType;
    private String msgType;
    private String msgTime;
    private String wxPackgeName;
    private String qqPackgeName;
    private static ArrayAdapter<Spanned> msgAdapter;

    public static Handler msgHandler;
    public static Integer notifyId=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        TextView textView_sender;
        ImageButton imageButton_send;
        ImageView imgMsgType;

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //hide activity title
        setFinishOnTouchOutside(true);//

        msgSave = DemoApplication.getInstance().getMsgSave();//?????????????????????Application??????????????????(?????????????????????????????????????????????//private final Map<String, List<Spanned>> msgSave = new HashMap<>();
        openHelper = DemoApplication.getInstance().getOpenHelper();
        currentUserList = DemoApplication.getInstance().getCurrentUserList();//???????????????????????????????????????????????????ArrayList<User>
        msgCountMap = DemoApplication.getInstance().getMsgCountMap();//Map<String, Integer> msgIdMap

        msgHandler = new Handler(){//????????????msg??????????????????UpdateMsgList?????????????????????
            @Override
            public void handleMessage(Message msg) {
                String handlerMsg = (String)msg.obj;

                if(handlerMsg.equals("UpdateMsgList") && msgAdapter!=null){
                    msgAdapter.notifyDataSetChanged();
                    msgListView.setSelection(msgSave.get(msgId).size());
                }

                super.handleMessage(msg);
            }
        };

        Intent intent = this.getIntent();
        Bundle msgDialogBundle = intent.getExtras();

        notifyId = msgDialogBundle.getInt("notifyId");
        msgId = msgDialogBundle.getString("msgId");
        msgTitle =msgDialogBundle.getString("msgTitle");
        msgBody =msgDialogBundle.getString("msgBody");
        senderType =msgDialogBundle.getString("senderType");
        msgType =msgDialogBundle.getString("msgType");
        msgTime =msgDialogBundle.getString("msgTime");
        if(msgDialogBundle.containsKey("qqPackgeName")) qqPackgeName =msgDialogBundle.getString("qqPackgeName");
        if(msgDialogBundle.containsKey("wxPackgeName"))  wxPackgeName =msgDialogBundle.getString("wxPackgeName");


        //???????????????????????????????????????????????????????????????
        if(msgCountMap.get(notifyId)!=null)
            msgCountMap.put(notifyId, 0);
        if (notifyId != -1) {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notifyId);
        }

        setContentView(R.layout.activity_dialog);

        //????????????????????????
        for(int    i=0;    i<currentUserList.size();    i++){
            if(currentUserList.get(i).getUserId().equals(msgId)){
                currentUserList.get(i).setMsgCount("0");
                if(MainActivity.userHandler!=null)
                    new userThread().start();//???????????????ui??????
                break;
            }
        }

        //???????????????????????????????????????????????????
        //SharedPreferences Settings = getSharedPreferences(PREF, Context.MODE_PRIVATE);
        Boolean qqIsReply=mySettings.getBoolean("check_box_preference_qq_reply",true);
        Boolean wxIsReply=mySettings.getBoolean("check_box_preference_wx_reply",true);

        textView_sender = (TextView) findViewById(R.id.textView_sender);
        msgListView = (ListView) findViewById(R.id.msg_list_view);
        imgMsgType = (ImageView) findViewById(R.id.msgType_imageView);
        imageButton_send = (ImageButton) findViewById(R.id.imagebutton_send);
        editText_content = (EditText) findViewById(R.id.edittext_content);
        LinearLayout msgListLinearLayout = (LinearLayout) findViewById(R.id.msg_list_ll);
        TextView sysTextView = (TextView) findViewById(R.id.msgType_text);
        line_view = findViewById(R.id.msgType_line_view);

        //???????????????????????????Listview???????????????????????????Textview
        if(msgId.equals("0")) {
            msgListLinearLayout.setVisibility(View.GONE);
            imageButton_send.setVisibility(View.GONE);
            line_view.setVisibility(View.GONE);
            sysTextView.setVisibility(View.VISIBLE);
            editText_content.clearFocus();
            sysTextView.setText(getString(R.string.text_user_welcome_msg));
        }

        //???????????????????????????????????????
        editText_content.setFocusable(true);
        editText_content.setFocusableInTouchMode(true);

        switch (msgType){
            case QQ:
                imgMsgType.setImageResource(R.mipmap.qq);
                if(!qqIsReply) {
                    imageButton_send.setEnabled(false);
                    editText_content.setEnabled(false);
                    editText_content.setText(getString(R.string.text_reply_disabled));
                    editText_content.clearFocus();
                    break;
                }
                if(isQqOnline==0) {
                //   imageButton_send.setEnabled(false);
                //   editText_content.setEnabled(false);
                   editText_content.setText(getString(R.string.text_check_login_failed));
                //   editText_content.clearFocus();
                //   break;
                }
                editText_content.requestFocus();
                editText_content.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(editText_content, InputMethodManager.SHOW_IMPLICIT);
                    }
                },300);
                break;
            case WEIXIN:
                imgMsgType.setImageResource(R.mipmap.weixin);
                if(!wxIsReply) {
                    imageButton_send.setEnabled(false);
                    editText_content.setEnabled(false);
                    editText_content.setText(getString(R.string.text_reply_disabled));
                    editText_content.clearFocus();
                    break;
                }
                if(isWxOnline==0) {
                    imageButton_send.setEnabled(false);
                   // editText_content.setEnabled(false);
                    editText_content.setText(getString(R.string.text_check_login_failed));
                  //  editText_content.clearFocus();
                  //  break;
                }
                editText_content.requestFocus();
                editText_content.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(editText_content, InputMethodManager.SHOW_IMPLICIT);
                    }
                },300);
                break;
            default:
                //??????????????????QQ???????????????????????????
                switch (msgId) {
                    case "0":
                        imgMsgType.setImageResource(R.mipmap.pin);
                        imageButton_send.setEnabled(false);
                        editText_content.setEnabled(false);
                        break;
                    case "1":
                        imgMsgType.setImageResource(R.mipmap.qq);
                        break;
                    case "2":
                        imgMsgType.setImageResource(R.mipmap.weixin);
                        break;
                    default:
                        imgMsgType.setImageResource(R.mipmap.pin);
                }
                editText_content.setHint(R.string.text_system_control);
                editText_content.clearFocus();
        }


        //???????????????????????????????????????????????????????????????
       if(msgSave.get(msgId)==null) {

           List<Spanned> msgList = new ArrayList<>();
           String str    =    "";
           switch (msgType) {
               case QQ:
                   str    =    "<font color='"+qqColor+"'><small>"+ msgTime +"</small></font><br>";
                   break;
               case WEIXIN:
                   str    =    "<font color='"+wxColor+"'><small>"+ msgTime +"</small></font><br>";
                   break;
               case SYS:
                   str    =    "<font color='"+sysReceiveColor+"'><small>"+ msgTime +"</small></font><br>";
                   break;
           }

           if(!msgBody.equals(getString(R.string.text_chat_initiative))) {
               msgList.add(toSpannedMessage(str + msgBody));

           }else {
               msgList.add(toSpannedMessage(""));
           }
           msgSave.put(msgId, msgList);
        }

        textView_sender.setText(msgTitle); //????????????
        msgAdapter = new ArrayAdapter<>(com.message_receiverAL.mm.DialogActivity.this,R.layout.dialog_msglist_item,R.id.text_message_item,msgSave.get(msgId));
        msgListView.setAdapter(msgAdapter);


        editText_content.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMsgAction();
                    editText_content.requestFocus();
                    return true;
                }

                return false;
            }
        });

        imageButton_send.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        //????????????????????????ID??????-1??????????????????????????????????????????
        notifyId =-1;
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        //????????????????????????ID??????-1??????????????????????????????????????????
        notifyId =-1;
        super.onStop();
    }

    @Override
    protected void onPause() {
        //????????????????????????ID??????-1??????????????????????????????????????????
        notifyId =-1;
        super.onPause();
    }

    public void sendMsgAction() {
        if(editText_content.getText().toString().length()==0)
        {
            return;
        }
        String sendResult = sendMessage(editText_content.getText().toString(), msgId, senderType, msgType);
        String isSucess;
        switch (sendResult) {
            case "????????????":
                isSucess = "";
                break;
            case "success":
                isSucess = "";
                break;
            default:
                if(sendResult.contains("success")){
                    isSucess = "";
                } else {
                    //??????????????????????????????????????????
                    switch (msgId) {
                        case "1":
                            if(editText_content.getText().toString().toLowerCase().contains("help")){
                                isSucess = "";
                            } else if (editText_content.getText().toString().toLowerCase().contains("relogin")) {
                                isSucess = "[!"+getString(R.string.text_send_failed)+":"+sendResult+"] ";
                            } else if (editText_content.getText().toString().toLowerCase().contains("stop")) {
                                isSucess = "[!"+getString(R.string.text_send_failed)+":"+sendResult+"] ";
                            }
                            else {
                                isSucess = "[!"+sendResult+"] ";
                            }
                            break;
                        case "2":
                            if(editText_content.getText().toString().toLowerCase().contains("help")){
                                isSucess = "";
                            } else if (editText_content.getText().toString().toLowerCase().contains("relogin")) {
                                isSucess = "[!"+getString(R.string.text_send_failed)+":"+sendResult+"] ";
                            } else if (editText_content.getText().toString().toLowerCase().contains("stop")) {
                                isSucess = "[!"+getString(R.string.text_send_failed)+":"+sendResult+"] ";
                            }
                            else {
                                isSucess = "[!"+sendResult+"] ";
                            }
                            break;
                        default:
                            isSucess = "[!"+sendResult+"] ";
                    }
                }
                break;
        }

        //?????????????????????????????????
        Spanned mySendMsg;
        String str = getColorMsgTime(msgType,true);
        mySendMsg=toSpannedMessage( str + isSucess + editText_content.getText().toString());
        if(msgSave.get(msgId)==null) {
            List<Spanned> msgList = new ArrayList<>();
            msgList.add(mySendMsg);
            msgSave.put(msgId,msgList);
        } else {
            List<Spanned> msgList=msgSave.get(msgId);
            msgList.add(mySendMsg);
            msgSave.put(msgId,msgList);
        }

        //?????????????????????????????????
        User currentUser = new User(msgTitle, msgId, msgType,isSucess+editText_content.getText().toString(), getCurTime(), senderType, notifyId,"0");
        for(int    i=0;    i<currentUserList.size();    i++){
            if(currentUserList.get(i).getUserId().equals(msgId)){
                currentUserList.remove(i);
                break;
            }
        }
        currentUserList.add(0,currentUser);

        //????????????????????????
        if(MainActivity.userHandler!=null)
            new userThread().start();

        msgAdapter.notifyDataSetChanged();
        msgListView.setSelection(msgSave.get(msgId).size());

        //??????????????????
        String content = editText_content.getText().toString().toLowerCase();
        if(sendResult.contains("success")){
            switch(msgId){
                case "1":
                    switch(content){
                        case "relogin":
                            MessageUtilDo(this,msgId,SYS,"1",getString(R.string.user_bot_qq_name),getString(R.string.text_control_relogin_in_progress),"0");
                            break;
                        case "stop":
                            MessageUtilDo(this,msgId,SYS,"1",getString(R.string.user_bot_qq_name),getString(R.string.text_control_stop_in_progress),"0");
                            break;
                    }
                    break;
                case "2":
                    switch(content){
                        case "relogin":
                            MessageUtilDo(this,msgId,SYS,"1",getString(R.string.user_bot_wechat_name),getString(R.string.text_control_relogin_in_progress),"0");
                            break;
                        case "stop":
                            MessageUtilDo(this,msgId,SYS,"1",getString(R.string.user_bot_wechat_name),getString(R.string.text_control_stop_in_progress),"0");
                            break;
                    }
                    break;
            }
        } else {
            switch(msgId){
                case "1":
                    switch(content){
                        case "help":
                            MessageUtilDo(this,msgId,SYS,"1",getString(R.string.user_bot_qq_name),getString(R.string.text_control_help),"0");
                            break;
                        case "relogin":
                            break;
                        case "stop":
                            break;
                        default:
                            MessageUtilDo(this,msgId,SYS,"1",getString(R.string.user_bot_qq_name),getString(R.string.text_control_unsupported),"0");
                            break;
                    }
                    break;
                case "2":
                    switch(content){
                        case "help":
                            MessageUtilDo(this,msgId,SYS,"1",getString(R.string.user_bot_wechat_name),getString(R.string.text_control_help),"0");
                            break;
                        case "relogin":
                            break;
                        case "stop":
                            break;
                        default:
                            MessageUtilDo(this,msgId,SYS,"1",getString(R.string.user_bot_wechat_name),getString(R.string.text_control_unsupported),"0");
                            break;
                    }
                    break;
            }
        }

        //?????????????????????????????????
        if(isSucess.equals("")) {
            editText_content.setText("");
        } else if (isSucess.equals(getString(R.string.text_send_success))){
            editText_content.setText("");
        }
        //    DialogActivity.this.finish();


    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //case R.id.imagebutton_cancel:
            //    DialogActivity.this.finish();
            //    break;
            case R.id.imagebutton_send:
                sendMsgAction();
                break;
        }
    }

    private String sendMessage(String msgSend, String msgId , String senderType, String msgType) {

        SharedPreferences Settings = getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String urlServer="";
        String urlType="";
        String urlQX="";
        String urlSend;

        HashMap<String, String> msgSendRequest = new HashMap<>();

        Boolean validationRequired=false;
        String validationSalt="";

        //?????????SYS???????????????????????????????????????????????????????????????????????????????????????????????????
        switch(msgId){
            case "1":
                //QQ???????????????
                msgType = QQ;
                break;
            case "2":
                //?????????????????????
                msgType = WEIXIN;
                break;
            default:
                //???????????????????????????
                break;
        }

        switch(msgType) {
            case QQ:
                urlServer=Settings.getString("edit_text_preference_qq_replyurl","");
                urlQX="openqq";
                if(Settings.getBoolean("check_box_preference_qq_validation",false)) {
                    validationRequired = true;
                    validationSalt=Settings.getString("edit_text_preference_qq_salt","");
                }
                break;
            case WEIXIN:
                urlServer=Settings.getString("edit_text_preference_wx_replyurl","");
                urlQX="openwx";
                if(Settings.getBoolean("check_box_preference_wx_validation",false)) {
                    validationRequired = true;
                    validationSalt=Settings.getString("edit_text_preference_wx_salt","");
                }
                break;
        }

        switch (msgId) {
            case "1":
                //QQ???????????????
                switch (msgSend) {
                    case "relogin":
                        //????????????mojo
                        urlType="/"+urlQX+"/"+"relogin";
                        break;
                    case "stop":
                        //??????Mojo
                        urlType="/"+urlQX+"/"+"stop_client";
                        break;
                    default:
                        //???????????????????????????????????????????????????
                        //urlType="/"+urlQX+"/";
                        return getString(R.string.text_control_unsupported_short);
                }
                break;
            case "2":
                //?????????????????????
                switch (msgSend) {
                    case "relogin":
                        //????????????mojo
                        urlType="/"+urlQX+"/"+"relogin";
                        break;
                    case "stop":
                        //??????Mojo
                        urlType="/"+urlQX+"/"+"stop_client";
                        break;
                    default:
                        //???????????????????????????????????????????????????
                        //urlType="/"+urlQX+"/";
                        return getString(R.string.text_control_unsupported_short);
                }
                break;
            default:
                switch (senderType) {
                    case "1":
                        urlType="/"+urlQX+"/send_friend_message";
                        break;
                    case "2":
                        urlType="/"+urlQX+"/send_group_message";
                        break;
                    case "3":
                        urlType="/"+urlQX+"/send_discuss_message";
                        break;
                }
                msgSendRequest.put("id",msgId);
                msgSendRequest.put("content",msgSend);
                if(validationRequired) {
                    String sign="";
                    try{
                        sign=getMD5(msgSend+msgId+validationSalt);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    msgSendRequest.put("sign",sign);
                }
                break;
        }

        Log.e("sendmsg","sending");
        urlSend=urlServer+urlType;
        Log.e("sendmsg",urlSend);
        return doGetRequestResutl(urlSend,msgSendRequest);

    }

    public static String getMD5(String val) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(val.getBytes("UTF-8"));
        byte[] magnitude = digest.digest();
        BigInteger bi = new BigInteger(1, magnitude);
        return String.format("%0" + (magnitude.length << 1) + "x", bi);
    }

    //???????????????????????????
    //?????????public static??????ReplyService?????? ???doGetRequestResutl(urlSend,msgSendRequest);???
    public static String doGetRequestResutl(final String URL, final HashMap<String, String> data){

        String sendResultJson="";
        String sendResult="";
        ExecutorService threadPool =  Executors.newSingleThreadExecutor();
        Future<String> future =
                threadPool.submit(
                        new Callable<String>() {
                            public String call() throws Exception {
                               //Thread.sleep(2000);
                               return NetUtil.doGetRequest(URL, data);
                            }
                        }
                );
        try {
            sendResultJson =  future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //??????????????????
        try
        {
            JSONObject jsonObject = new JSONObject(sendResultJson);
            sendResult = jsonObject.getString("status");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return sendResult;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        setIntent(intent);
    }

    //??????????????????????????????
    public void onTitleClick(View v) {
        switch (v.getId()){
            case R.id.title_relativeLayout:
                //System.out.println("?????????????????????");
                switch (msgType) {
                    case QQ:
                        //??????QQ
                        Intent intentNewQq = this.getPackageManager().getLaunchIntentForPackage(qqPackgeName);
                        if (intentNewQq != null) {
                            this.startActivity(intentNewQq);
                        }  else {
                            Toast.makeText(this.getApplicationContext(), "????????????"+qqPackgeName, Toast.LENGTH_LONG).show();
                        }
                        break;
                    case WEIXIN:
                        //????????????
                        Intent intentNewWx = this.getPackageManager().getLaunchIntentForPackage(wxPackgeName);
                        if (intentNewWx != null) {
                            this.startActivity(intentNewWx);
                        }  else {
                            Toast.makeText(this.getApplicationContext(), "????????????"+wxPackgeName, Toast.LENGTH_LONG).show();
                        }
                    case SYS:
                        //???????????????
                        Intent intentNewSys = new Intent(this, MainActivity.class);
                        this.startActivity(intentNewSys);
                        break;
                }
                break;
        }
    }

// --Commented out by Inspection START (2017/2/27 19:24):
//    public Handler getHandler(){
//        return msgHandler;
//    }
// --Commented out by Inspection STOP (2017/2/27 19:24)

    //???????????????ui??????
    class userThread extends Thread {
        @Override
        public void run() {
            Message msg = new Message();
            msg.obj = "UpdateCurrentUserList";
            MainActivity.userHandler.sendMessage(msg);
            super.run();
        }
    }
}
