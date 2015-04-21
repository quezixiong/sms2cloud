package com.cumulitech.sms2cloud;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;

/**
 * Created by harley on 15/4/12.
 */
public class BindActivity extends Activity {
    private TextView promptTextView;
    private static final String tag = "BindActivity";
    private ImageView qrImageView;


    protected void onCreate(Bundle savedInstanceState){
       super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind);
        promptTextView = (TextView)findViewById(R.id.prompt_textView);
        qrImageView = (ImageView) findViewById(R.id.qr_imageView);
        //check internet connection.
        if(!new Utility(this).hasInternet()){
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
            promptTextView.setText("请先联网");
        }else{
            GetBindQRTask gbt = new GetBindQRTask(this);
            gbt.execute();
        }
    }
}
