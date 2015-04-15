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
import java.nio.charset.Charset;

/**
 * Created by harley on 15/4/14.
 */
public class ServerHandler {
    private static final String Tag = "ServerHandler";
    private Context context;
    private String number;
    private String serialNumber;
    private static final String sendMsgURL = "http://123.57.46.246/user/message";
    private static final String bindServerURL = "http://123.57.46.246/user/bind";


    public ServerHandler(Context context){
        this.context = context;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        number = sp.getString("number", null);
        serialNumber = sp.getString("serialNumber", null);
    }

    public static Boolean bindServer(String number, String serialNumber) throws IOException{
        try {
            String url = String.format(bindServerURL);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("number", number);
            jsonObj.put("serialNumber", serialNumber);
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
                return errcode == 0;
            }
        } catch (JSONException e) {
            Log.e(Tag, "Exception", e);
        }
        return false;
    }

    public Boolean sendMsg(String msg) throws IOException{
        try {
            String url = String.format(sendMsgURL);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("message", new String(msg.getBytes(Charset.forName("UTF-8")), Charset.forName("UTF-8")));
            jsonObj.put("number", number);
            jsonObj.put("serialNumber", serialNumber);
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
                return errcode == 0;
            }
        } catch (JSONException e) {
            Log.e(Tag, "Exception", e);
        }
        return false;
    }
}
