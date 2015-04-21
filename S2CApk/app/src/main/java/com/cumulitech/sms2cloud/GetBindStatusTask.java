package com.cumulitech.sms2cloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.AbstractCollection;

/**
 * Created by harley on 15/4/20.
 */
public class GetBindStatusTask extends AsyncTask<String, String, Boolean >{
    private Context context;

    public GetBindStatusTask(Context context){
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int expireIn = sp.getInt("expireIn", 0);
        int count = 10;
        try {
            while (true) {
                Thread.sleep(1000);
                boolean isBind = new ServerHandler(context).getBindStatus();
                if(isBind){
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("isBind", isBind);
                    editor.apply();
                    return true;
                }
                if(count++ >= expireIn / 1000){
                    TextView promptTextView = (TextView) ((Activity)context).findViewById(R.id.prompt_textView);
                    ImageView qrImageView = (ImageView) ((Activity)context).findViewById(R.id.qr_imageView);
                    promptTextView.setText("二维码已经过期");
                    qrImageView.setVisibility(View.INVISIBLE);
                    return false;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if(success){
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
        }
    }
}
