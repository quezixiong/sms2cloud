//package com.cumulitech.sms2cloud;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.preference.PreferenceManager;
//import android.util.Log;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.*;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.net.URLEncoder;
//import java.nio.charset.Charset;
//
///**
// * Created by harley on 15/4/8.
// */
//public class Wechat {
//    private SharedPreferences myData;
//
//    private String appId;
//    private String appSecret;
//    private String openId;
//    private String accessToken;
//    private long updateTime;
//
//    private static final String token_api = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
//    private static final String msg_api = "https://api.weixin.qq.com/cgi-bin/message/mass/preview?access_token=%s";
//    private static final String bind_api = "http://123.57.46.246/wechat/android/data?token=%s";
//
//    public Wechat(Context context) {
//        myData = PreferenceManager.getDefaultSharedPreferences(context);
//        appId = myData.getString("appId", "");
//        appSecret = myData.getString("appSecret", "");
//        openId = myData.getString("openId", "");
//        accessToken = myData.getString("accessToken", "");
//        updateTime = myData.getLong("updateTime", 0);
//    }
//
//    // TODO: Internet error tips should be added.
//    public String getToken() throws IOException {
//        if(this.accessToken != null && System.currentTimeMillis() - updateTime <= 7140)
//            return this.accessToken;
//        else{
//            String url = String.format(token_api, URLEncoder.encode(appId, "UTF-8"), URLEncoder.encode(appSecret, "UTF-8"));
//            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
//            InputStream response = connection.getInputStream();
//            int status = connection.getResponseCode();
//            if(status==200){
//                try{
//                    String result = "";
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(response, "UTF-8"));
//                    for (String line; (line = reader.readLine()) != null;) {
//                        result += line;
//                    }
//                    JSONObject obj = new JSONObject(result);
//                    accessToken = obj.getString("access_token");
//                    if(accessToken != null && !accessToken.equals("")){
//                        updateTime = System.currentTimeMillis();
//                        SharedPreferences.Editor editor = myData.edit();
//                        editor.putString("accessToken", accessToken);
//                        editor.putLong("updateTime", updateTime);
//                        editor.apply();
//                    }
//                    return accessToken;
//                }catch (Exception e){
//                    Log.e("Tag","Exception", e);
//                }
//            }
//            return null;
//        }
//    }
//
//    public static String[] bindWechat(String token) throws Exception {
//        String url = String.format(Wechat.bind_api, URLEncoder.encode(token, "UTF-8"));
//        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
//        InputStream response = connection.getInputStream();
//        int status = connection.getResponseCode();
//        if (status == 200) {
//            try {
//                String result = "";
//                BufferedReader reader = new BufferedReader(new InputStreamReader(response, "UTF-8"));
//                for (String line; (line = reader.readLine()) != null; ) {
//                    result += line;
//                }
//                JSONObject obj = new JSONObject(result);
//                String appId = obj.getString("app_id");
//                String appSecrect = obj.getString("app_secret");
//                String openId = obj.getString("open_id");
//                String name = obj.getString("name");
//                if (appId != null && appSecrect != null && openId != null) {
//                    return new String[]{appId, appSecrect, openId, name};
//                }
//            } catch (Exception e) {
//                Log.e("Tag", "Exception", e);
//            }
//        }
//        return null;
//    }
//
//
//    public Boolean send_msg(String msg) throws IOException{
//        String token = this.getToken();
//        try {
//            String url = String.format(msg_api, token);
//            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
//            connection.setDoOutput(true);
//            connection.setRequestProperty("Content-Type", "text/plain;charset=utf-8");
//            JSONObject tmp_obj = new JSONObject();
//            tmp_obj.put("content", new String(msg.getBytes(Charset.forName("UTF-8")), Charset.forName("UTF-8")));
//            JSONObject obj = new JSONObject();
//            obj.put("text", tmp_obj);
//            obj.put("touser", this.openId);
//            obj.put("msgtype", "text");
//            String payload = obj.toString();
//
//            OutputStream output = connection.getOutputStream();
//            output.write(payload.getBytes("UTF-8"));
//            InputStream response = connection.getInputStream();
//            int status = connection.getResponseCode();
//            if (status == 200) {
//                String result = "";
//                BufferedReader reader = new BufferedReader(new InputStreamReader(response, "UTF-8"));
//                for (String line; (line = reader.readLine()) != null; ) {
//                    result += line;
//                    JSONObject r_obj = new JSONObject(result);
//                    int errcode = r_obj.getInt("errcode");
//                    if (errcode == 0) return true;
//                    else return false;
//                }
//            }
//        } catch (JSONException e) {
//            Log.e("Tag", "Exception", e);
//        }
//        return false;
//    }
//}
