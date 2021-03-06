package com.ebusbar.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Jelly on 2016/3/3.
 */
public class SharedPreferencesUtil {

    private static String TAG = "SharedPreferencesUtil";

    private static SharedPreferencesUtil sharedPreferencesUtil =  new SharedPreferencesUtil();

    public static SharedPreferencesUtil getInstance(){
        return sharedPreferencesUtil;
    }

    private SharedPreferencesUtil(){}

    /**
     * SharedPreferences保存用户对象
     * @param context
     * @param object
     */
    public void saveObject(Context context,Object object){
        try {
            Log.v(TAG,object.getClass().getName());
            SharedPreferences sharedPreferences = context.getSharedPreferences(object.getClass().getName(), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            String objectBase64 = new String(Base64.encodeBase64(baos.toByteArray()));
            editor.putString(object.getClass().getName(),objectBase64);
            editor.commit();//提交修改
        } catch (IOException e) {
            Log.v(TAG,"缓存失败");
            e.printStackTrace();
        }
    }

    /**
     * 保存字符串
     * @param context
     * @param fileName
     * @param value
     */
    public void saveString(Context context,String fileName,String key,String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.commit();
    }

    /**
     * 读取字符串
     * @param context
     * @param fileName
     * @param key
     * @return
     */
    public String readString(Context context,String fileName,String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName,Context.MODE_PRIVATE);
        String result = sharedPreferences.getString(key, "");
        return  result;
    }

    /**
     * 通过SharedPreferences读取对象
     * @param context
     * @param fileName
     * @return
     */
    public Object readObject(Context context,String fileName){
        Object object = null;
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
            if(!sharedPreferences.contains(fileName)){ //如果不存在直接返回
                return null;
            }
            String objectBase64 = sharedPreferences.getString(fileName, "");
            byte[] base64Bytes = Base64.decodeBase64(objectBase64.getBytes());
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            object = ois.readObject();
            return object;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除SharePreference里面缓存的对象
     * @param context
     * @param fileName
     */
    public void clearObject(Context context,String fileName){
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().commit();
    }
}
