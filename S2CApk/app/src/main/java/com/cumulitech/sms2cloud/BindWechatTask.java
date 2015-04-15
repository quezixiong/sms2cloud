//package com.cumulitech.sms2cloud;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.AsyncTask;
//import android.preference.PreferenceManager;
//import android.util.Log;
//import android.widget.Button;
//
///**
// * Created by harley on 15/4/9.
// */
//public class BindWechatTask extends AsyncTask <String, String, Boolean>{
//    private String token;
//    private Activity bindActivity;
//
//    public BindWechatTask(Activity bindActivity, String token){
//        this.bindActivity = bindActivity;
//        this.token = token;
//    }
//
//    @Override
//    protected Boolean doInBackground(String... strings) {
//        try{
//            String result[] = Wechat.bindWechat(token);
//            if(result != null){
//                SharedPreferences myData = PreferenceManager.getDefaultSharedPreferences(bindActivity);
//                SharedPreferences.Editor editor = myData.edit();
//                editor.putString("appId", result[0]);
//                editor.putString("appSecret", result[1]);
//                editor.putString("openId", result[2]);
//                editor.putString("name", result[3]);
//                editor.putBoolean("isBind", true);
//                editor.apply();
//                return true;
//            }
//            else return false;
//        }catch (Exception e){
//            Log.e("BindWechatTask","exception", e);
//            return false;
//        }
//    }
//
//    @Override
//    protected void onPostExecute(Boolean success) {
//        super.onPostExecute(success);
//        if(success) {
//            Intent intent = new Intent(bindActivity, MainActivity.class);
//            intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
//            bindActivity.startActivity(intent);
//        }else{
//            Button b = (Button)this.bindActivity.findViewById(R.id.confirm_button);
//            b.setText("failed");
//        }
//
//    }
//}
