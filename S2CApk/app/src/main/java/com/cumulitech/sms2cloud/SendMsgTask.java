package com.cumulitech.sms2cloud;

import android.app.Activity;
import android.app.Service;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by harley on 15/4/8.
 */
public class SendMsgTask extends AsyncTask<String, String, String> {

//    private Activity context;
    private String msg;
    private ServerHandler serverHandler;


    public SendMsgTask(ServerHandler serverHandler, String msg){
        this.serverHandler = serverHandler;
        this.msg = msg;
    }

    @Override
    protected String doInBackground(String... strings) {
        String result = "";
        try{
            if(serverHandler.sendMsg(this.msg)) result = "success";
            else result = "failed";
        }catch (Exception e) {
            Log.e("Tag", "Exception", e);
        }
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}
