package com.cumulitech.sms2cloud;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class MsgMonitorService extends Service {
    private static final String TAG = "MsgMonitorService";
    private Uri SMS_INBOX = Uri.parse("content://sms/");
    public static final String BROADCAST_ACTION = "com.cumulitech.sms2cloud.app";

    private MsgObserver msgObserver;
    public Handler msgHandler = new Handler();
    private ServerHandler serverHandler;
    private Intent bcIntent;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        msgObserver = new MsgObserver(this, msgHandler);
        getContentResolver().registerContentObserver(SMS_INBOX, true, msgObserver);

        bcIntent = new Intent(BROADCAST_ACTION);
        serverHandler = new ServerHandler(this);
    }

    private String getSmsFromPhone() {
        ContentResolver cr = getContentResolver();
        String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};//"_id", "address", "person",, "date", "type
        Cursor cur = cr.query(SMS_INBOX, projection, null, null, "date desc");
        if (null == cur)
            return null;
        boolean has_msg = cur.moveToNext();
        if (has_msg) {
            String number = "号码:" + cur.getString(cur.getColumnIndex("address")) + "\n";//手机号
            String name = "姓名:" + cur.getString(cur.getColumnIndex("person")) + "\n";//联系人姓名列表
            String body = "内容:" + cur.getString(cur.getColumnIndex("body")) + "\n";
            String msg = number + name + body;
            return msg;
        }
        return null;
    }


    class MsgObserver extends ContentObserver {

        public MsgObserver(Context context, Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //每当有新短信到来时，使用我们获取短消息的方法
            String msg = getSmsFromPhone();
            if(msg != null){
                SendMsgTask smt = new SendMsgTask(serverHandler, msg);
                smt.execute();
                bcIntent.putExtra("msg", msg);
                sendBroadcast(bcIntent);
            }
        }
    }
}
