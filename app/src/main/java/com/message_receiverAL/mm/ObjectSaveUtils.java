package com.message_receiverAL.mm;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

public class ObjectSaveUtils {
    /**
     * 保存聊天记录
     * @param context
     * @param name
     * @param saveMsg
     */
    public static void saveHashSpanned(Context context, String name, Map<String, List<Spanned>> saveMsg) throws Exception{
        Map<String, List<String>> saveMsgString =new HashMap<>();
        Set<String> keySet=saveMsg.keySet();
        Iterator<String> it=keySet.iterator();
        List<String> list_tmp;
        String key;
        while(it.hasNext()){
            list_tmp=new ArrayList<>();
            key=it.next();
            List<Spanned> value=saveMsg.get(key);
            for(Spanned s:value){
                list_tmp.add(Html.toHtml(s));
            }
            saveMsgString.put(key,list_tmp);
        }
        saveObject(context,name,saveMsgString);
    }
    public static Map<String, List<Spanned>> getHashSpanned(Context context,String name) throws Exception{
        Map<String, List<Spanned>> saveMsg=null;
        Map<String, List<String>> saveMsgString =(Map<String, List<String>>)getObject(context,name);
        String key;
        if(saveMsgString!=null){
            saveMsg=new HashMap<>();
            Set<String> keySet =saveMsgString.keySet();
            Iterator<String> it=keySet.iterator();
            List<Spanned> list_tmp;
            while(it.hasNext()){
                list_tmp=new ArrayList<>();
                key=it.next();
                List<String> value=saveMsgString.get(key);
                for(String s:value){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        list_tmp.add(Html.fromHtml(s,FROM_HTML_MODE_COMPACT));
                    } else {
                        list_tmp.add(Html.fromHtml(s));
                    }
                }
                saveMsg.put(key,list_tmp);
            }
        }
        return saveMsg;
    }

    /**
     * 保存对象
     * @param context
     * @param name
     * @param obj
     */
    public static void saveObject(Context context, String name, Object obj) throws Exception{
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = context.openFileOutput(name, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // fos流关闭异常
                    e.printStackTrace();
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    // oos流关闭异常
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取对象
     * @param context   上下文
     * @param name      KEY
     * @return   返回对象，没有对象返回空
     */
    public static Object getObject(Context context, String name) throws Exception{
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = context.openFileInput(name);
            ois = new ObjectInputStream(fis);
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // fis流关闭异常
                    e.printStackTrace();
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    // ois流关闭异常
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}

