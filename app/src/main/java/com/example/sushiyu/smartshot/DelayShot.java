package com.example.sushiyu.smartshot;

import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DelayShot extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String DELAYSHOT_TAG = "mcfire_delayshot";
    public static int max_shot_times_abpoint;/*save after abpoint setting*/
    public static int max_shot_times;/*received from mcu*/
    private String mDeviceName;
    private String mDeviceAddress;
    public BluetoothLeService mBluetoothLeService;
    private Intent connect_intent;
    private TextView mConnectionState;
    private boolean mConnected = false;
    boolean connect_status_bit=false;
    private Handler mHandler;

    private EditText EtHour;
    private EditText EtMinute;
    private EditText EtSecond;
    private EditText EtShotTimes;
    private EditText EtBaoguangHour;
    private EditText EtBaoguangMin;
    private EditText EtBaoguangSec;
    private TextView TvShottimeTotal;
    private TextView TvRemainTimes;
    private Switch switch_direction;
    private Switch switch_dingdian;
    private ImageButton delayshot_btn_left;
    private ImageButton delayshot_btn_right;
    private ImageButton delayshot_btn_start;
    private boolean delayshot_start_press_flag = false;

    private String StrParam[] = new String[8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        setContentView(R.layout.activity_delay_shot);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        /*if (DelayShot.max_shot_times == 0)
        {
            DelayShot.max_shot_times = 100;
        }
        Log.e(DELAYSHOT_TAG, "max shot time"+ DelayShot.max_shot_times);
        */
        Calendar c = Calendar.getInstance();

        String month = Integer.toString(c.get(Calendar.MONTH));
        String day = Integer.toString(c.get(Calendar.DAY_OF_MONTH));
        String hour = Integer.toString(c.get(Calendar.HOUR_OF_DAY));
        String min = Integer.toString(c.get(Calendar.MINUTE));
        Log.e(DELAYSHOT_TAG, "Calendar获取当前日期"+"2018"+"年"+month+"月"+day+"日"+hour+":"+min);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        boolean sg;
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        sg = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);



        TvShottimeTotal = (TextView) findViewById(R.id.shottime_total);
        TvRemainTimes = (TextView) findViewById(R.id.shot_times_remain);
        switch_direction = (Switch) findViewById(R.id.switch2_direction);
        switch_dingdian = (Switch) findViewById(R.id.switch3_dingdian);
        EtHour = (EditText) findViewById(R.id.delayshot_hour_id);
        EtHour.setText("00");
        EtHour.setSelection(EtHour.getText().length());
        EtHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EtHour.setText(EtHour.getText().toString());// 添加这句后实现效果
                EtHour.selectAll();
            }
        });
        StrParam[0] = EtHour.getText().toString();
        EtHour.setOnFocusChangeListener(new View.OnFocusChangeListener(){

            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                if(hasFocus){
                    Log.e(DELAYSHOT_TAG, "EtHour has focus");

                }else{
                    Log.e(DELAYSHOT_TAG, "no focus");
                    int number = Integer.parseInt(EtHour.getText().toString());
                    if (number < 10)
                    {
                        EtHour.setText("0"+number);
                    }
                    Log.e(DELAYSHOT_TAG, "DDD + "+number);
                    Log.e(DELAYSHOT_TAG, "DDD1");
                    //StrParam[0] = EtHour.getText().toString();
                    StrParam[0] = String.format("%02d", number);
                    Log.e(DELAYSHOT_TAG, "DDD2");
                    String tx_string;
                    tx_string="0093010201"+StrParam[2]+StrParam[1]+StrParam[0];
                    Log.e(DELAYSHOT_TAG, "DDD3");
                    int total_time,hour,min,sec,tmp;
                    int PhotoNum;
                    PhotoNum = Integer.parseInt(StrParam[3]);
                    Log.e(DELAYSHOT_TAG, "DDD4 "+PhotoNum);
                    if(PhotoNum < 1)
                    {
                        PhotoNum = 1;
                        Log.e(DELAYSHOT_TAG, "DDD41 "+PhotoNum);
                    }
                    total_time = (PhotoNum-1)
                            * ((Integer.parseInt(StrParam[4]) * 3600 +
                            Integer.parseInt(StrParam[5]) * 60 +
                            Integer.parseInt(StrParam[6]))+(Integer.parseInt(StrParam[0]) * 3600 +
                            Integer.parseInt(StrParam[1]) * 60 +
                            Integer.parseInt(StrParam[2]))) ;
                    Log.e(DELAYSHOT_TAG, "DDD5");
                    tmp = total_time;
                    hour = tmp / 3600;
                    tmp -= hour*3600;
                    min = tmp / 60;
                    tmp -=min*60;
                    sec = tmp%60;

                    Log.e(DELAYSHOT_TAG, "EEE");
                    TvShottimeTotal.setText(String.format("%02d", hour)+":"
                            +String.format("%02d", min)+":"+String.format("%02d", sec));
                    if(!connect_status_bit)
                        return;
                    Log.e(DELAYSHOT_TAG, "FFF");
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(DELAYSHOT_TAG, tx_string);
                }
            }

        });
        EtHour.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int selectionStart;
            private int selectionEnd;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                temp = charSequence;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str_second;
                Log.e(DELAYSHOT_TAG, "EditText Hour Step1+ " + temp.length());
                selectionStart = EtHour.getSelectionStart();
                selectionEnd = EtHour.getSelectionEnd();
                if (temp.length() > 2) {
                    Log.e(DELAYSHOT_TAG, "EditText Hour, Text Length > 2");
                    editable.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    str_second = "59";
                    EtHour.setText(str_second);
                    EtHour.setSelection(tempSelection);
                }

                Log.e(DELAYSHOT_TAG, "EditText Hour Step2");
                if (editable.length() == 0)
                {
                    Log.e(DELAYSHOT_TAG, "EditText Hour NULL Editable");
                    return;
                }
                Log.e(DELAYSHOT_TAG, "EditText Hour Step3");


                int number = Integer.parseInt(editable.toString());
                if (number > 59)
                {
                    Log.e(DELAYSHOT_TAG, "EtHour larger than 59");
                    EtHour.setText("59");
                }
                Log.e(DELAYSHOT_TAG, "EditText Hour Step4");

            }
        });

        EtMinute = (EditText) findViewById(R.id.delayshot_minute_id);
        EtMinute.setText("00");
        EtMinute.setSelection(EtMinute.getText().length());
        EtMinute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EtMinute.setText(EtMinute.getText().toString());// 添加这句后实现效果
                EtMinute.selectAll();
            }
        });
        StrParam[1] = EtMinute.getText().toString();
        EtMinute.setOnFocusChangeListener(new View.OnFocusChangeListener(){

            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                if(hasFocus){
                    Log.e(DELAYSHOT_TAG, "EtMinute has focus");

                }else{
                    Log.e(DELAYSHOT_TAG, "no focus");
                    int number = Integer.parseInt(EtMinute.getText().toString());
                    if (number < 10)
                    {
                        EtMinute.setText("0"+number);
                    }
                    StrParam[1] = String.format("%02d", number);
                    String tx_string;
                    tx_string="0093010201"+StrParam[2]+StrParam[1]+StrParam[0];
                    int total_time,hour,min,sec,tmp;
                    total_time = (Integer.parseInt(StrParam[3])-1)
                            * ((Integer.parseInt(StrParam[4]) * 3600 +
                            Integer.parseInt(StrParam[5]) * 60 +
                            Integer.parseInt(StrParam[6]))+(Integer.parseInt(StrParam[0]) * 3600 +
                            Integer.parseInt(StrParam[1]) * 60 +
                            Integer.parseInt(StrParam[2]))) ;
                    tmp = total_time;
                    hour = tmp / 3600;
                    tmp -= hour*3600;
                    min = tmp / 60;
                    tmp -=min*60;
                    sec = tmp%60;
                    TvShottimeTotal.setText(String.format("%02d", hour)+":"
                            +String.format("%02d", min)+":"+String.format("%02d", sec));
                    if(!connect_status_bit)
                        return;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(DELAYSHOT_TAG, tx_string);
                }
            }

        });
        EtMinute.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int selectionStart;
            private int selectionEnd;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                temp = charSequence;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str_second;
                selectionStart = EtMinute.getSelectionStart();
                selectionEnd = EtMinute.getSelectionEnd();

                if (temp.length() > 2) {
                    editable.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    str_second = "59";
                    EtMinute.setText(str_second);
                    EtMinute.setSelection(tempSelection);
                }
                Log.e(DELAYSHOT_TAG, "XXX"+editable.toString());
                //Log.e("delayshot", "H + "+editable.toString());
                if (editable.length() == 0)
                {
                    Log.e(DELAYSHOT_TAG, "NULL Editable");
                    return;
                }

                int number = Integer.parseInt(editable.toString());
                if (number > 59)
                {
                    Log.e(DELAYSHOT_TAG, "EtMinute larger than 59");
                    EtMinute.setText("59");
                }
            }

        });

        EtSecond = (EditText) findViewById(R.id.delayshot_second_id);
        EtSecond.setText("00");
        EtSecond.setSelection(EtSecond.getText().length());
        EtSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EtSecond.setText(EtSecond.getText().toString());// 添加这句后实现效果
                EtSecond.selectAll();
            }
        });
        StrParam[2] = EtSecond.getText().toString();
        EtSecond.setOnFocusChangeListener(new View.OnFocusChangeListener(){

            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                if(hasFocus){
                    Log.e(DELAYSHOT_TAG, "EtSecond has focus");

                }else{
                    Log.e(DELAYSHOT_TAG, "no focus");
                    int number = Integer.parseInt(EtSecond.getText().toString());
                    if (number < 10)
                    {
                        EtSecond.setText("0"+number);
                    }
                    //StrParam[2] = EtSecond.getText().toString();
                    StrParam[2] = String.format("%02d", number);

                    String tx_string;
                    tx_string="0093010201"+StrParam[2]+StrParam[1]+StrParam[0];
                    int total_time,hour,min,sec,tmp;
                    total_time = (Integer.parseInt(StrParam[3])-1)
                            * ((Integer.parseInt(StrParam[4]) * 3600 +
                            Integer.parseInt(StrParam[5]) * 60 +
                            Integer.parseInt(StrParam[6]))+(Integer.parseInt(StrParam[0]) * 3600 +
                            Integer.parseInt(StrParam[1]) * 60 +
                            Integer.parseInt(StrParam[2]))) ;
                    tmp = total_time;
                    hour = tmp / 3600;
                    tmp -= hour*3600;
                    min = tmp / 60;
                    tmp -=min*60;
                    sec = tmp%60;
                    TvShottimeTotal.setText(String.format("%02d", hour)+":"
                            +String.format("%02d", min)+":"+String.format("%02d", sec));
                    if(!connect_status_bit)
                        return;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(DELAYSHOT_TAG, tx_string);
                }
            }

        });
        EtSecond.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int selectionStart;
            private int selectionEnd;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                temp = charSequence;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str_second;
                selectionStart = EtSecond.getSelectionStart();
                selectionEnd = EtSecond.getSelectionEnd();
                if (temp.length() > 2) {
                    editable.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    str_second = "59";
                    EtSecond.setText(str_second);
                    EtSecond.setSelection(tempSelection);
                }
                if (editable.length() == 0)
                {
                    Log.e(DELAYSHOT_TAG, "NULL Editable");
                    return;
                }
                int number = Integer.parseInt(editable.toString());
                if (number > 59)
                {
                    Log.e(DELAYSHOT_TAG, "EtSecond larger than 59");
                    EtSecond.setText("59");
                }
            }
        });


        EtShotTimes = (EditText) findViewById(R.id.delayshot_shot_times);
        EtShotTimes.setText("00");
        EtShotTimes.setSelection(EtShotTimes.getText().length());
        EtShotTimes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EtShotTimes.setText(EtShotTimes.getText().toString());// 添加这句后实现效果
                EtShotTimes.selectAll();
            }
        });
        StrParam[3] = EtShotTimes.getText().toString();
        EtShotTimes.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int selectionStart;
            private int selectionEnd;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                temp = charSequence;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                selectionStart = EtShotTimes.getSelectionStart();
                selectionEnd = EtShotTimes.getSelectionEnd();
                Log.e(DELAYSHOT_TAG, "abc step1");
                if (temp.length() > 5) {
                    Log.e(DELAYSHOT_TAG, "abc step11 start"+selectionStart+" end "+selectionEnd);
                    editable.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    EtShotTimes.setText(editable);
                    EtShotTimes.setSelection(tempSelection);
                    Log.e(DELAYSHOT_TAG, "temp.length() > 5");
                }
                Log.e(DELAYSHOT_TAG, "abc step2");
                if (editable.length() == 0)
                {
                    Log.e(DELAYSHOT_TAG, "NULL Editable");
                    return;
                }
                Log.e(DELAYSHOT_TAG, "abc step3");
                int number = Integer.parseInt(editable.toString());
                Log.e(DELAYSHOT_TAG, "abc step4");
                if (number < 1)
                {
                    EtShotTimes.setText("01");
                    Log.e(DELAYSHOT_TAG, "number < 1");
                }
                Log.e(DELAYSHOT_TAG, "abc step5");
                if (number > DelayShot.max_shot_times_abpoint)
                {
                    Log.e(DELAYSHOT_TAG, "number > DelayShot.max_shot_times_abpoint"
                            +number+":"+DelayShot.max_shot_times_abpoint);
                    number = DelayShot.max_shot_times_abpoint;
                    EtShotTimes.setText(""+number);


                }
                Log.e(DELAYSHOT_TAG, "abc step6");
                //StrParam[3] = EtShotTimes.getText().toString();
                StrParam[3] = String.format("%04d", number);


                String tx_string;
                /*
                if (number >= 0 && number <0x10)
                {
                    tx_string="0093010203000"+ Integer.toHexString(number)+"0000";
                }
                else if (number >= 0x10 && number <0x100)
                {
                    tx_string="009301020300"+ Integer.toHexString(number)+"0000";
                }
                else if (number >= 0x100 && number <0x1000)
                {
                    tx_string="00930102030"+ Integer.toHexString(number)+"0000";
                }
                else if (number >= 0x1000 && number <0x27f0)
                {
                    tx_string="0093010203"+ Integer.toHexString(number)+"0000";
                }
                else
                {
                    tx_string="";
                }
                */

                String shot_time_low = String.format("%04x", number).substring(2,4);
                String shot_time_high = String.format("%04x", number).substring(0,2);
                Log.e(DELAYSHOT_TAG, "abc step7");
                tx_string="0093010203"+ shot_time_low + shot_time_high +"00";
                Log.e(DELAYSHOT_TAG, "tx_string " + tx_string);
                int total_time,hour,min,sec,tmp;
                total_time = (Integer.parseInt(StrParam[3])-1)
                        * ((Integer.parseInt(StrParam[4]) * 3600 +
                        Integer.parseInt(StrParam[5]) * 60 +
                        Integer.parseInt(StrParam[6]))+(Integer.parseInt(StrParam[0]) * 3600 +
                        Integer.parseInt(StrParam[1]) * 60 +
                        Integer.parseInt(StrParam[2]))) ;
                tmp = total_time;
                hour = tmp / 3600;
                tmp -= hour*3600;
                min = tmp / 60;
                tmp -=min*60;
                sec = tmp%60;
                if (total_time >= 0)
                {
                    TvShottimeTotal.setText(String.format("%02d", hour)+":"
                            +String.format("%02d", min)+":"+String.format("%02d", sec));
                }
                Log.e(DELAYSHOT_TAG, "abc step8");
                if(!connect_status_bit)
                    return;
                mBluetoothLeService.txxx(tx_string);
                Log.e(DELAYSHOT_TAG, tx_string);
                Log.e(DELAYSHOT_TAG, "AAAAAAAAAAAA");

            }
        });

        EtBaoguangHour = (EditText) findViewById(R.id.delayshot_baoguang_hour_id);
        EtBaoguangHour.setText("00");
        EtBaoguangHour.setSelection(EtBaoguangHour.getText().length());
        EtBaoguangHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EtBaoguangHour.setText(EtBaoguangHour.getText().toString());// 添加这句后实现效果
                EtBaoguangHour.selectAll();
            }
        });
        StrParam[4] = EtBaoguangHour.getText().toString();
        EtBaoguangHour.setOnFocusChangeListener(new View.OnFocusChangeListener(){

            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                if(hasFocus){
                    Log.e(DELAYSHOT_TAG, "EtBaoguangHour has focus");

                }else{
                    Log.e(DELAYSHOT_TAG, "no focus");
                    int number = Integer.parseInt(EtBaoguangHour.getText().toString());
                    if (number < 10)
                    {
                        EtBaoguangHour.setText("0"+number);
                    }
                    //StrParam[4] = EtBaoguangHour.getText().toString();
                    StrParam[4] = String.format("%02d", number);
                    String tx_string;
                    tx_string="0093010202"+StrParam[6]+StrParam[5]+StrParam[4];
                    int total_time,hour,min,sec,tmp;
                    total_time = (Integer.parseInt(StrParam[3])-1)
                            * ((Integer.parseInt(StrParam[4]) * 3600 +
                            Integer.parseInt(StrParam[5]) * 60 +
                            Integer.parseInt(StrParam[6]))+(Integer.parseInt(StrParam[0]) * 3600 +
                            Integer.parseInt(StrParam[1]) * 60 +
                            Integer.parseInt(StrParam[2]))) ;
                    tmp = total_time;
                    hour = tmp / 3600;
                    tmp -= hour*3600;
                    min = tmp / 60;
                    tmp -=min*60;
                    sec = tmp%60;
                    TvShottimeTotal.setText(String.format("%02d", hour)+":"
                            +String.format("%02d", min)+":"+String.format("%02d", sec));
                    if(!connect_status_bit)
                        return;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(DELAYSHOT_TAG, tx_string);
                }
            }

        });
        EtBaoguangHour.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int selectionStart;
            private int selectionEnd;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                temp = charSequence;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str_second;
                selectionStart = EtHour.getSelectionStart();
                selectionEnd = EtHour.getSelectionEnd();
                Log.e(DELAYSHOT_TAG, "EtBaoguangHour step1");
                if (temp.length() > 2) {
                    Log.e(DELAYSHOT_TAG, "EtBaoguangHour temp.length() > 2");
                    Log.e(DELAYSHOT_TAG, "EtBaoguangHour selectionStart = "+selectionStart);
                    Log.e(DELAYSHOT_TAG, "EtBaoguangHour selectionEnd = "+selectionEnd);
                    editable.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    str_second = "59";
                    EtBaoguangHour.setText(str_second);
                    EtBaoguangHour.setSelection(tempSelection);
                }
                Log.e(DELAYSHOT_TAG, "EtBaoguangHour step2");
                if (editable.length() == 0)
                {
                    Log.e(DELAYSHOT_TAG, "NULL Editable");
                    return;
                }
                int number = Integer.parseInt(editable.toString());
                Log.e(DELAYSHOT_TAG, "EtBaoguangHour step3 number = "+number);
                if (number > 59)
                {
                    Log.e(DELAYSHOT_TAG, "EtBaoguangHour larger than 59");
                    EtBaoguangHour.setText("59");
                }
                Log.e(DELAYSHOT_TAG, "EtBaoguangHour step4");
            }
        });

        EtBaoguangMin = (EditText) findViewById(R.id.delayshot_baoguang_minute_id);
        EtBaoguangMin.setText("00");
        EtBaoguangMin.setSelection(EtBaoguangMin.getText().length());
        EtBaoguangMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EtBaoguangMin.setText(EtBaoguangMin.getText().toString());// 添加这句后实现效果
                EtBaoguangMin.selectAll();
            }
        });
        StrParam[5] = EtBaoguangMin.getText().toString();
        EtBaoguangMin.setOnFocusChangeListener(new View.OnFocusChangeListener(){

            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                if(hasFocus){
                    Log.e(DELAYSHOT_TAG, "EtBaoguangMin has focus");

                }else{
                    Log.e(DELAYSHOT_TAG, "no focus");
                    int number = Integer.parseInt(EtBaoguangMin.getText().toString());
                    if (number < 10)
                    {
                        EtBaoguangMin.setText("0"+number);
                    }
                    //StrParam[5] = EtBaoguangMin.getText().toString();
                    StrParam[5] = String.format("%02d", number);
                    String tx_string;
                    tx_string="0093010202"+StrParam[6]+StrParam[5]+StrParam[4];
                    int total_time,hour,min,sec,tmp;
                    total_time = (Integer.parseInt(StrParam[3])-1)
                            * ((Integer.parseInt(StrParam[4]) * 3600 +
                            Integer.parseInt(StrParam[5]) * 60 +
                            Integer.parseInt(StrParam[6]))+(Integer.parseInt(StrParam[0]) * 3600 +
                            Integer.parseInt(StrParam[1]) * 60 +
                            Integer.parseInt(StrParam[2]))) ;
                    tmp = total_time;
                    hour = tmp / 3600;
                    tmp -= hour*3600;
                    min = tmp / 60;
                    tmp -=min*60;
                    sec = tmp%60;
                    TvShottimeTotal.setText(String.format("%02d", hour)+":"
                            +String.format("%02d", min)+":"+String.format("%02d", sec));
                    if(!connect_status_bit)
                        return;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(DELAYSHOT_TAG, tx_string);
                }
            }

        });
        EtBaoguangMin.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int selectionStart;
            private int selectionEnd;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                temp = charSequence;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str_second;
                selectionStart = EtBaoguangMin.getSelectionStart();
                selectionEnd = EtBaoguangMin.getSelectionEnd();
                if (temp.length() > 2) {
                    Log.e(DELAYSHOT_TAG, "EtBaoguangMin selectionStart = "+selectionStart);
                    Log.e(DELAYSHOT_TAG, "EtBaoguangMin selectionEnd = "+selectionEnd);
                    editable.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    str_second = "59";
                    EtBaoguangMin.setText(str_second);
                    EtBaoguangMin.setSelection(tempSelection);
                }
                if (editable.length() == 0)
                {
                    Log.e(DELAYSHOT_TAG, "NULL Editable");
                    return;
                }
                int number = Integer.parseInt(editable.toString());
                if (number > 59)
                {
                    Log.e(DELAYSHOT_TAG, "EtBaoguangMin larger than 59");
                    EtBaoguangMin.setText("59");
                }
            }
        });

        EtBaoguangSec = (EditText) findViewById(R.id.delayshot_baoguang_second_id);
        EtBaoguangSec.setText("00");
        EtBaoguangSec.setSelection(EtBaoguangSec.getText().length());
        EtBaoguangSec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EtBaoguangSec.setText(EtBaoguangSec.getText().toString());// 添加这句后实现效果
                EtBaoguangSec.selectAll();
            }
        });
        StrParam[6] = EtBaoguangSec.getText().toString();
        EtBaoguangSec.setOnFocusChangeListener(new View.OnFocusChangeListener(){

            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                if(hasFocus){
                    Log.e(DELAYSHOT_TAG, "EtBaoguangSec has focus");

                }else{
                    Log.e(DELAYSHOT_TAG, "no focus");
                    int number = Integer.parseInt(EtBaoguangSec.getText().toString());
                    if (number < 10)
                    {
                        EtBaoguangSec.setText("0"+number);
                    }
                    //StrParam[6] = EtBaoguangSec.getText().toString();
                    StrParam[6] = String.format("%02d", number);
                    String tx_string;
                    tx_string="0093010202"+StrParam[6]+StrParam[5]+StrParam[4];
                    int total_time,hour,min,sec,tmp;
                    total_time = (Integer.parseInt(StrParam[3])-1)
                            * ((Integer.parseInt(StrParam[4]) * 3600 +
                            Integer.parseInt(StrParam[5]) * 60 +
                            Integer.parseInt(StrParam[6]))+(Integer.parseInt(StrParam[0]) * 3600 +
                            Integer.parseInt(StrParam[1]) * 60 +
                            Integer.parseInt(StrParam[2]))) ;
                    tmp = total_time;
                    hour = tmp / 3600;
                    tmp -= hour*3600;
                    min = tmp / 60;
                    tmp -=min*60;
                    sec = tmp%60;
                    TvShottimeTotal.setText(String.format("%02d", hour)+":"
                            +String.format("%02d", min)+":"+String.format("%02d", sec));
                    if(!connect_status_bit)
                        return;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(DELAYSHOT_TAG, tx_string);
                }
            }

        });
        EtBaoguangSec.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int selectionStart;
            private int selectionEnd;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                temp = charSequence;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str_second;
                selectionStart = EtBaoguangSec.getSelectionStart();
                selectionEnd = EtBaoguangSec.getSelectionEnd();
                if (temp.length() > 2) {
                    editable.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    str_second = "59";
                    EtBaoguangSec.setText(str_second);
                    EtBaoguangSec.setSelection(tempSelection);
                }
                if (editable.length() == 0)
                {
                    Log.e(DELAYSHOT_TAG, "NULL Editable");
                    return;
                }
                int number = Integer.parseInt(editable.toString());
                if (number > 59)
                {
                    Log.e(DELAYSHOT_TAG, "EtBaoguangSec larger than 59");
                    EtBaoguangSec.setText("59");
                }

            }
        });

        switch_direction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    String tx_string;
                    tx_string="0093010205000000";
                    if(!connect_status_bit)
                        return ;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(DELAYSHOT_TAG, tx_string);
                }else {
                    String tx_string;
                    tx_string="0093010205ff0000";
                    if(!connect_status_bit)
                        return ;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(DELAYSHOT_TAG, tx_string);
                }
            }
        });

        switch_dingdian.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    String tx_string;
                    tx_string="0093010204ff0000";
                    if(!connect_status_bit)
                        return ;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(DELAYSHOT_TAG, tx_string);
                }else {
                    String tx_string;
                    tx_string="0093010204000000";
                    if(!connect_status_bit)
                        return ;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(DELAYSHOT_TAG, tx_string);
                }
            }
        });


        delayshot_btn_start = (ImageButton) findViewById(R.id.img_btn_delayshot_start);
        //delayshot_btn_start.setBackgroundResource(R.drawable.stop);
        delayshot_btn_start.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    if (delayshot_start_press_flag) {
                        delayshot_btn_start.setBackgroundResource(R.drawable.stop);
                        delayshot_start_press_flag = false;

                        String tx_string;
                        tx_string="0093010206ff0000";
                        if(!connect_status_bit)
                            return false;
                        mBluetoothLeService.txxx(tx_string);
                        Log.e(DELAYSHOT_TAG, tx_string);

                    }else{
                        delayshot_btn_start.setBackgroundResource(R.drawable.start);
                        delayshot_start_press_flag = true;
                        //send_msg_to_mcu();

                        String tx_string;
                        tx_string="0093010206000000";
                        if(!connect_status_bit)
                            return false;
                        mBluetoothLeService.txxx(tx_string);
                        Log.e(DELAYSHOT_TAG, tx_string);
                    }
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){

                }
                return false;
            }
        });
        //updateConnectionState(R.string.connecting);
        Log.e(DELAYSHOT_TAG, "连接中");
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.e(DELAYSHOT_TAG, "delayshot onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(DELAYSHOT_TAG, "Unable to initialize Bluetooth");
                finish();
            }
            boolean result;
            result = mBluetoothLeService.connect(mDeviceAddress);
            Log.e(DELAYSHOT_TAG, "delayshot result "+result);
            if (result)
            {
                mConnected = true;
                connect_status_bit=true;
                timer.cancel();
                Log.e(DELAYSHOT_TAG, "delayshot connected!");
                mBluetoothLeService.txxx("0003010200000000");
                Log.e(DELAYSHOT_TAG, "fuck!!!");
                delay(20);

            }
            // Automatically connects to the device upon successful start-up initialization.
            /*
            if (mBluetoothLeService.isconnect())
            {
                connect_status_bit=true;
                mConnected = true;
                Log.e(DELAYSHOT_TAG, "delayshot connected!");
            }
            else
            {
                boolean result;
                result = mBluetoothLeService.connect(mDeviceAddress);
                Log.e(DELAYSHOT_TAG, "delayshot connect "+result);
                if (result)
                {
                    mConnected = true;
                    connect_status_bit=true;
                    timer.cancel();
                    Log.e(DELAYSHOT_TAG, "delayshot connected!");
                    mBluetoothLeService.txxx("0003010200000000");
                    Log.e(DELAYSHOT_TAG, "0003010200000000");
                }
            }*/
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.e(DELAYSHOT_TAG, "delayshot BroadcastReceiver");
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                connect_status_bit=true;
                Log.e(DELAYSHOT_TAG, "delayshot ACTION_GATT_CONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connect_status_bit=true;
                mConnected = true;
                mBluetoothLeService.disconnect();
                unbindService(mServiceConnection);
                mBluetoothLeService = null;
                timer.cancel();
                timer=null;
                Intent intent1 = new Intent(DelayShot.this,
                        MainActivity.class);
                startActivity(intent1);
                connect_status_bit=false;
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.e(DELAYSHOT_TAG, "delayshot service discovered");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.e(DELAYSHOT_TAG, "delayshot received data");
                String str = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.e(DELAYSHOT_TAG, "str"+str);
                Log.e(DELAYSHOT_TAG, "substr"+str.substring(0,4));
                if (str.substring(0,4).equals("0309"))
                {
                    Log.e(DELAYSHOT_TAG, "hello");
                    int remain_times = Integer.valueOf(str.substring(4,6),16);
                    /*
                    if (remain_times == DelayShot.max_shot_times)
                    {
                        if (delayshot_start_press_flag) {
                            delayshot_btn_start.setBackgroundResource(R.drawable.start);
                            delayshot_start_press_flag = false;

                        }else{
                            delayshot_btn_start.setBackgroundResource(R.drawable.stop);
                            delayshot_start_press_flag = true;
                        }
                    }*/
                    TvRemainTimes.setText(""+remain_times);
                }

                if (str.substring(0,4).equals("0302"))
                {
                    String tmp_str = str.substring(4,6);


                    /*zhuge second*/
                    StrParam[2] = ""+tmp_str;
                    EtSecond.setText(""+tmp_str);
                    Log.e(DELAYSHOT_TAG, "zhuge second = "+tmp_str);
                    /*zhuge minute*/
                    tmp_str = str.substring(6,8);
                    StrParam[1] = ""+tmp_str;
                    EtMinute.setText(""+tmp_str);
                    Log.e(DELAYSHOT_TAG, "zhuge minute = "+tmp_str);
                    /*zhuge hour*/
                    tmp_str = str.substring(8,10);
                    StrParam[0] = ""+tmp_str;
                    EtHour.setText(""+tmp_str);
                    Log.e(DELAYSHOT_TAG, "zhuge hour = "+tmp_str);
                    /*baoguang second*/
                    tmp_str = str.substring(10,12);
                    StrParam[6] = ""+tmp_str;
                    EtBaoguangSec.setText(""+tmp_str);
                    Log.e(DELAYSHOT_TAG, "baoguang second = "+tmp_str);
                    /*baoguang minute*/
                    tmp_str = str.substring(12,14);
                    StrParam[5] = ""+tmp_str;
                    EtBaoguangMin.setText(""+tmp_str);
                    Log.e(DELAYSHOT_TAG, "baoguang minute = "+tmp_str);
                    /*baoguang hour*/
                    tmp_str = str.substring(14,16);
                    StrParam[4] = ""+tmp_str;
                    EtBaoguangHour.setText(""+tmp_str);
                    Log.e(DELAYSHOT_TAG, "baoguang hour = "+tmp_str);
                    /*shot maxt time*/
                    tmp_str = str.substring(16,18);
                    Log.e(DELAYSHOT_TAG, "Integer.valueOf(str.substring(18,20),16) "+Integer.valueOf(str.substring(18,20),16));
                    Log.e(DELAYSHOT_TAG, "Integer.valueOf(tmp_str,16) "+Integer.valueOf(tmp_str,16));
                    Log.e(DELAYSHOT_TAG, "max shoot time integer = "+((Integer.valueOf(str.substring(18,20),16))*256 +
                            Integer.valueOf(tmp_str,16)));
                    StrParam[3] = ""+((Integer.valueOf(str.substring(18,20),16))*256 +
                            Integer.valueOf(tmp_str,16));
                    DelayShot.max_shot_times = Integer.valueOf(tmp_str,16);
                    Log.e(DELAYSHOT_TAG, "DelayShot.max_shot_times " + DelayShot.max_shot_times);
                    Log.e(DELAYSHOT_TAG, "StrParam[3] "+StrParam[3]);
                    EtShotTimes.setText(""+StrParam[3]);
                    /*auto back*/
                    tmp_str = str.substring(20,22);
                    Log.e(DELAYSHOT_TAG, "auto back = "+tmp_str);
                    /*direction*/
                    tmp_str = str.substring(24,26);
                    Log.e(DELAYSHOT_TAG, "direction = "+tmp_str);
                    if (tmp_str.equals("FF"))
                    {
                        switch_direction.setChecked(false);
                    }
                    else if (tmp_str.equals("00"))
                    {
                        switch_direction.setChecked(true);
                    }
                    /*start or stop*/
                    tmp_str = str.substring(26,28);
                    if (tmp_str.equals("FF"))
                    {
                        delayshot_btn_start.setBackgroundResource(R.drawable.stop);
                        delayshot_start_press_flag = false;
                    }
                    else if (tmp_str.equals("00"))
                    {
                        delayshot_btn_start.setBackgroundResource(R.drawable.start);
                        delayshot_start_press_flag = true;
                    }

                    int total_time,hour,min,sec,tmp;
                    total_time = (Integer.parseInt(StrParam[3])-1)
                            * ((Integer.parseInt(StrParam[4]) * 3600 +
                            Integer.parseInt(StrParam[5]) * 60 +
                            Integer.parseInt(StrParam[6]))+(Integer.parseInt(StrParam[0]) * 3600 +
                            Integer.parseInt(StrParam[1]) * 60 +
                            Integer.parseInt(StrParam[2]))) ;
                    tmp = total_time;
                    hour = tmp / 3600;
                    tmp -= hour*3600;
                    min = tmp / 60;
                    tmp -=min*60;
                    sec = tmp%60;
                    Log.e(DELAYSHOT_TAG, "total_time "+total_time);
                    Log.e(DELAYSHOT_TAG, "StrParam[0] "+StrParam[0]);
                    Log.e(DELAYSHOT_TAG, "StrParam[1] "+StrParam[1]);
                    Log.e(DELAYSHOT_TAG, "StrParam[2] "+StrParam[2]);
                    Log.e(DELAYSHOT_TAG, "StrParam[3] "+StrParam[3]);
                    Log.e(DELAYSHOT_TAG, "StrParam[4] "+StrParam[4]);
                    Log.e(DELAYSHOT_TAG, "StrParam[5] "+StrParam[5]);
                    Log.e(DELAYSHOT_TAG, "StrParam[6] "+StrParam[6]);
                    if(total_time >= 0)
                    {
                        TvShottimeTotal.setText(String.format("%02d", hour)+":"
                                +String.format("%02d", min)+":"+String.format("%02d", sec));
                    }

                }
                else
                {
                    Log.e(DELAYSHOT_TAG, "not equal to 030Bff");
                }

                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    return false;
                }
            };

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        //mDataField.setText(R.string.no_data);
    }

    Timer timer = new Timer();

    public void delay(int ms){
        try {
            Thread.currentThread();
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            if (msg.what == 1) {
                //Log.e("delayshot", "handleMessage");
                //tvShow.setText(Integer.toString(i++));
                //scanLeDevice(true);
                if (mBluetoothLeService != null) {
                    //Log.e("delayshot", "mBluetoothLeService != null");
                    if( mConnected==false )
                    {
                        Log.e(DELAYSHOT_TAG,"mConnected==false");
                        //updateConnectionState(R.string.connecting);
                        final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                        Log.e(DELAYSHOT_TAG, "Connect request result=" + result);
                    }
                }
            }
            super.handleMessage(msg);
        };
    };
    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            //Log.e("delayshot", "BBB");
            // ÐèÒª×öµÄÊÂ:·¢ËÍÏûÏ¢
            //Log.e("delayshot", "timer task run");
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.vedio_shot, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.vedio_shoot) {

            Intent intent1 = new Intent(DelayShot.this,
                    VedioShot.class);
            intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                    mDeviceName);
            intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                    mDeviceAddress);
            startActivity(intent1);
            // Handle the camera action
        } else if (id == R.id.delay_shoot) {

            Log.e(DELAYSHOT_TAG, "nav_gallery");

        } else if (id == R.id.abpoint) {
            Log.e(DELAYSHOT_TAG, "nav_slideshow");
            Intent intent1 = new Intent(DelayShot.this,
                    ABpoint.class);
            intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                    mDeviceName);
            intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                    mDeviceAddress);
            startActivity(intent1);

        }else if (id == R.id.software_upgrade) {

        } else if (id == R.id.support) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void shiftLanguage(String sta){

        if(sta.equals("zh")){
            Locale.setDefault(Locale.US);
            Configuration config = getBaseContext().getResources().getConfiguration();
            config.locale = Locale.US;
            getBaseContext().getResources().updateConfiguration(config
                    , getBaseContext().getResources().getDisplayMetrics());
            refreshSelf();
        }else{
            Locale.setDefault(Locale.CHINESE);
            Configuration config = getBaseContext().getResources().getConfiguration();
            config.locale = Locale.CHINESE;
            getBaseContext().getResources().updateConfiguration(config
                    , getBaseContext().getResources().getDisplayMetrics());
            refreshSelf();
        }
    }
    //refresh self
    public void refreshSelf(){
        Intent intent=new Intent(this,DelayShot.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        Log.e(DELAYSHOT_TAG, "delayshot displayGattServices");
        if( gattServices.size()>0&&mBluetoothLeService.get_connected_status( gattServices )>=4 )
        {
            if( connect_status_bit )
            {
                mConnected = true;
                mBluetoothLeService.enable_JDY_ble(true);
                try {
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBluetoothLeService.enable_JDY_ble(true);
            }else{
                Log.e(DELAYSHOT_TAG, "delayshot displayGattServices disconnect");
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(DELAYSHOT_TAG, "delayshot onResume");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {

            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.e(DELAYSHOT_TAG, "delayshot onResume connect " + result);
        }
    }

    @Override
    protected void onPause() {
        Log.e(DELAYSHOT_TAG, "delayshot onPause");
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        Log.e(DELAYSHOT_TAG, "delayshot onDestroy");
        super.onDestroy();
        mBluetoothLeService.disconnect();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        timer.cancel();
        timer=null;
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        Log.e(DELAYSHOT_TAG, "IntentFilter");
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
