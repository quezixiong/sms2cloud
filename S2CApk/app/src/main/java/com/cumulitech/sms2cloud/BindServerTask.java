package com.cumulitech.sms2cloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by harley on 15/4/9.
 */
public class BindServerTask extends AsyncTask <String, String, Boolean>{
    private String number;
    private String serialNumber;
    private Context context;

    public BindServerTask(Context context, String number, String serialNumber){
        this.context = context;
        this.number = number;
        this.serialNumber = serialNumber;
    }

    @Override
    //TODO: clear try .... catch
    protected Boolean doInBackground(String... strings) {
        try{
            Boolean isSuccess = ServerHandler.bindServer(number, serialNumber);
            if(isSuccess){
                SharedPreferences myData = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = myData.edit();
                editor.putString("number", number);
                editor.putString("serialNumber", serialNumber);
                editor.putBoolean("isBind", true);
                editor.apply();
            }
            return(isSuccess);
        }catch (Exception e){
            Log.e("BindWechatTask","exception", e);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if(success) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }else{
            TextView tv = (TextView)((Activity)context).findViewById(R.id.prompt_textView);
            tv.setText("failed");
        }
    }
}
