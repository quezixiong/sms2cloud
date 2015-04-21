package com.cumulitech.sms2cloud;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import java.io.*;
import java.util.Properties;

/**
 * Created by harley on 15/4/19.
 */
public class AppSettings {
    public static final String tag = "AppSetting";
    public String serverUrl;
    public String getBindQRUrl;
    public String getBindStatusUrl;
    public String sendMsgURL;

    private static AppSettings instance = null;

    public AppSettings(Context context){
        AssetManager assetManager = context.getResources().getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open("config.properties");
            Properties prop = new Properties();
            prop.load(inputStream);
            serverUrl = prop.getProperty("serverUrl");
            getBindQRUrl = serverUrl + "/bind?iccid=%s";
            getBindStatusUrl = serverUrl + "/bind_status";
            sendMsgURL = serverUrl + "/message";
            instance = this;
        } catch (Resources.NotFoundException e) {
            Log.e(tag, "", e);
        } catch (IOException e) {
            Log.e(tag, "", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(tag, "", e);
                }
            }
        }
    }

    public static void init(Context context){
        new AppSettings(context);
    }

    public static AppSettings getInstance() throws Exception{
        if(instance == null){
            throw new Exception();
        }
        else{
            return instance;
        }
    }
}
