package com.cumulitech.sms2cloud;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by harley on 15/4/12.
 */
public class BindActivity extends Activity implements View.OnClickListener{
    private EditText numberEditText;
    private Button confirmButton;
    private TextView promptTextView;
    private String Tag = "BindActivity";
    private String number;
    private String serialNumber;


    protected void onCreate(Bundle savedInstanceState){
       super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind);
        confirmButton = (Button)findViewById(R.id.confirm_button);
        numberEditText = (EditText)findViewById(R.id.number_editText);
        promptTextView = (TextView)findViewById(R.id.prompt_textView);
        confirmButton.setOnClickListener(this);

        TelephonyManager tMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        number = tMgr.getLine1Number();
        serialNumber = tMgr.getSimSerialNumber();
        Log.e(Tag, number + "\n" + serialNumber);
        if(number != null && !number.equals("")){
            bindServer();
        }
    }

    @Override
    public void onClick(View view) {
        if(view == confirmButton){
            number = numberEditText.getText().toString();
            bindServer();
        }

    }

    private void bindServer(){
        Utility utility = new Utility(this);
        if(!utility.hasInternet()){
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
            return;
        }
        confirmButton.setVisibility(View.INVISIBLE);
        numberEditText.setVisibility(View.INVISIBLE);
        promptTextView.setText(R.string.wait);
        BindServerTask bst = new BindServerTask(this, number, serialNumber);
        bst.execute();
    }
}
