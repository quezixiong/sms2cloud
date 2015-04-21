package com.cumulitech.sms2cloud;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements View.OnClickListener {
    private Button switchButton;
    private TextView logTextview;
    private TextView helloTextview;
    private Intent msgServiceIntent;
    private Boolean isMonitorOn;
    private SharedPreferences myData;

    private BroadcastReceiver bcReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateLogUI(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // init AppSettings
        AppSettings.init(this);
        myData = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isBind = myData.getBoolean("isBind", false);
        if(isBind){
            setContentView(R.layout.activity_main);
            String hello = "你好" + myData.getString("number", null);
            helloTextview = (TextView)findViewById(R.id.helloTextview);
            helloTextview.setText(hello);
            switchButton = (Button)findViewById(R.id.switch_button);
            switchButton.setOnClickListener(this);
            logTextview = (TextView)findViewById(R.id.logTextView);
            logTextview.setVisibility(View.INVISIBLE);
            isMonitorOn = false;
            msgServiceIntent = new Intent();
            msgServiceIntent.setClass(MainActivity.this, MsgMonitorService.class);
            registerReceiver(bcReceiver, new IntentFilter(MsgMonitorService.BROADCAST_ACTION));
        } else{
            Intent intent = new Intent(this, BindActivity.class);
            startActivity(intent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if(view == switchButton){
            Utility utility = new Utility(this);
            if(!utility.hasInternet()){
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
                return;
            }
            if(isMonitorOn){
                switchButton.setText(R.string.start_monitor);
                logTextview.append("\n你关闭了监听短信...");
                isMonitorOn = false;
                stopService(msgServiceIntent);
            }else{
                switchButton.setText(R.string.stop_monitor);
                logTextview.setVisibility(View.VISIBLE);
                String log = myData.getString("log","");
                logTextview.setText(log);
                startService(msgServiceIntent);
                isMonitorOn = true;
                logTextview.append("\n你打开了监听短信");
            }

        }
    }

    private void updateLogUI(Intent intent){
        String msg = intent.getStringExtra("msg");
        logTextview.append(msg);
    }
}
