package com.cumulitech.sms2cloud;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;

/**
 * Created by harley on 15/4/17.
 */
public class GetBindQRTask extends AsyncTask<String, String, Bitmap>{
    private Context context;
    private static final String tag = "GetBindQRTask";

    public GetBindQRTask(Context context){
        this.context = context;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        TelephonyManager tMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String iccid = tMgr.getSimSerialNumber();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("iccid", iccid);
        try{
            String info[] = ServerHandler.getBindInfo(iccid);
            String qrUrl = info[0];
            String identifier = info[1];
            int expireIn = Integer.parseInt(info[2]);
            editor.putString("identifier", identifier);
            editor.putInt("expireIn", expireIn);
            editor.apply();
            return Utility.createQRImage(qrUrl);
        }catch (IOException e){
            Log.e(tag,"", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap qrBitmap) {
        super.onPostExecute(qrBitmap);
        ImageView qrImageView = (ImageView) ((Activity)context).findViewById(R.id.qr_imageView);
        qrImageView.setImageBitmap(qrBitmap);
        GetBindStatusTask gbst = new GetBindStatusTask(context);
        gbst.execute();
    }

}
