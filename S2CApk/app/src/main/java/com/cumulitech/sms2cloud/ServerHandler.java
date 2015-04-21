package com.cumulitech.sms2cloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * Created by harley on 15/4/14.
 */
public class ServerHandler {
//    private static final String sendMsgURL = serverURL + "message";
//    private static final String bindServerURL = serverURL + "/user/bind";
    private static final String tag = "ServerHandler";
    private String iccid;
    private String identifier;


    public ServerHandler(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        iccid = sp.getString("iccid", null);
        identifier = sp.getString("identifier", null);
    }

    //GET
    public static String[] getBindInfo(String iccid) throws IOException{
        try {
            AppSettings appSettings = AppSettings.getInstance();
            String url = String.format(appSettings.getBindQRUrl, URLEncoder.encode(iccid, "UTF-8"));
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            InputStream response = connection.getInputStream();
            int status = connection.getResponseCode();
            if (status == 200) {
                String result = "";
                BufferedReader reader = new BufferedReader(new InputStreamReader(response, "UTF-8"));
                for (String line; (line = reader.readLine()) != null; ) {
                    result += line;
                }
                JSONObject r_obj = new JSONObject(result);
                int errcode = r_obj.getInt("errcode");
                if(errcode == Utility.ErrorCode.OK){
                    String qrUrl = r_obj.getString("qr_url");
                    String identifier = r_obj.getString("identifier");
                    int expireIn = r_obj.getInt("expire_in");
                    return new String[]{qrUrl, identifier, Integer.toString(expireIn)};
                }
            }
        } catch (JSONException e) {
            Log.e(tag, "Exception", e);
        } catch (Exception e){
            Log.e(tag, "Exception", e);
        }
        return null;
    }

    public Boolean getBindStatus() throws IOException{
        try {
            AppSettings appSettings = AppSettings.getInstance();
            String url = String.format(appSettings.getBindStatusUrl, URLEncoder.encode(identifier, "UTF-8"));
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("iccid", iccid);
            String payload = jsonObj.toString();

            OutputStream output = connection.getOutputStream();
            output.write(payload.getBytes("UTF-8"));

            InputStream response = connection.getInputStream();
            int status = connection.getResponseCode();
            if (status == 200) {
                String result = "";
                BufferedReader reader = new BufferedReader(new InputStreamReader(response, "UTF-8"));
                for (String line; (line = reader.readLine()) != null; ) {
                    result += line;
                }
                JSONObject r_obj = new JSONObject(result);
                int errcode = r_obj.getInt("errcode");
                if(errcode == Utility.ErrorCode.OK){
                    return r_obj.getBoolean("is_bind");
                }
            }
        } catch (JSONException e) {
            Log.e(tag, "Exception", e);
        } catch (Exception e){
            Log.e(tag, "Exception", e);
        }
        return null;

    }

    public Boolean sendMsg(String msg) throws IOException{
        try {
            AppSettings appSettings = AppSettings.getInstance();
            String url = String.format(appSettings.sendMsgURL, URLEncoder.encode(identifier, "UTF-8"));
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("message", new String(msg.getBytes(Charset.forName("UTF-8")), Charset.forName("UTF-8")));
            jsonObj.put("iccid", iccid);
            String payload = jsonObj.toString();

            OutputStream output = connection.getOutputStream();
            output.write(payload.getBytes("UTF-8"));

            InputStream response = connection.getInputStream();
            int status = connection.getResponseCode();
            if (status == 200) {
                String result = "";
                BufferedReader reader = new BufferedReader(new InputStreamReader(response, "UTF-8"));
                for (String line; (line = reader.readLine()) != null; ) {
                    result += line;
                }
                JSONObject r_obj = new JSONObject(result);
                int errcode = r_obj.getInt("errcode");
                return errcode == Utility.ErrorCode.OK;
            }
        } catch (JSONException e) {
            Log.e(tag, "Exception", e);
        } catch (Exception e){
            Log.e(tag, "Exception", e);
        }
        return false;
    }
}
