package com.example.sushiyu.smartshot;

import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class VedioShot extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String VEDIOSHOT_TAG = "mcfire_vedio";
    private String mDeviceName;
    private String mDeviceAddress;
    public BluetoothLeService mBluetoothLeService;
    public boolean mConnected = false;
    private Intent connect_intent;
    public  boolean connect_status_bit=false;
    private TextView mConnectionState;
    private Handler mHandler;
    private SeekBar seekbar_speed;
    private TextView speed_current;
    private Switch mSwitch;
    private Switch mSwitch_danpai;
    private ImageButton img_btn_left;
    private ImageButton img_btn_right;
    private ImageButton img_btn_start;
    private boolean start_press_flag;
    private boolean get_param_success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        setContentView(R.layout.activity_vedio_shot_1);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        //mConnectionState = (TextView) findViewById(R.id.connection_state);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mHandler = new Handler();
        timer.schedule(task, 1000, 1000);
        boolean sg;
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        sg = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        //updateConnectionState(R.string.connecting);
        Log.e(VEDIOSHOT_TAG, "连接中");

        seekbar_speed = (SeekBar) findViewById(R.id.seekbar_speed);
        mSwitch = (Switch) findViewById(R.id.switch1);
        mSwitch_danpai = (Switch) findViewById(R.id.switch1_danpai);

        img_btn_left = (ImageButton) findViewById(R.id.imageButton_left);
        img_btn_right = (ImageButton) findViewById(R.id.imageButton_right);
        img_btn_start = (ImageButton) findViewById(R.id.imageButton_start);
        speed_current = (TextView) findViewById(R.id.speed_current);
        img_btn_left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    img_btn_left.setBackgroundResource(R.drawable.left_press);
                    img_btn_right.setBackgroundResource(R.drawable.right_release);
                    String tx_string;
                    tx_string="0093010102ff0000";
                    if (!connect_status_bit)
                        return false;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(VEDIOSHOT_TAG, tx_string);
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){

                }
                return false;
            }
        });

        img_btn_right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    img_btn_left.setBackgroundResource(R.drawable.left_release);
                    img_btn_right.setBackgroundResource(R.drawable.right_press);
                    String tx_string;
                    tx_string="0093010102000000";
                    if (!connect_status_bit)
                        return false;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(VEDIOSHOT_TAG, tx_string);
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){

                }
                return false;
            }
        });
        //img_btn_start.setBackgroundResource(R.drawable.stop);
        img_btn_start.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    if (start_press_flag) {
                        mSwitch_danpai.setClickable(true);
                        img_btn_start.setBackgroundResource(R.drawable.start);
                        start_press_flag = false;
                        String tx_string;
                        tx_string="0093010105000000";
                        if (!connect_status_bit)
                            return false;
                        mBluetoothLeService.txxx(tx_string);
                        Log.e(VEDIOSHOT_TAG, tx_string);
                    }else{
                        mSwitch_danpai.setClickable(false);
                        img_btn_start.setBackgroundResource(R.drawable.stop);
                        start_press_flag = true;
                        String tx_string;
                        tx_string="0093010105ff0000";
                        if (!connect_status_bit)
                            return false;
                        mBluetoothLeService.txxx(tx_string);
                        Log.e(VEDIOSHOT_TAG, tx_string);
                    }
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){

                }
                return false;
            }
        });
        seekbar_speed.setMax(99);

        seekbar_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int run_speed;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress_value, boolean fromUser) {
                run_speed = progress_value;
                speed_current.setText(""+(run_speed+1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(ABPointActivity.this, "start slice", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(ABPointActivity.this, "stop slice", Toast.LENGTH_SHORT).show();

                Log.e(VEDIOSHOT_TAG, "progress_value = "+run_speed);

                String tx_string;
                if (run_speed >= 100)
                {
                    run_speed = 99;
                }
                if (run_speed < 16)
                {

                    tx_string="00930101010"+ Integer.toHexString(run_speed)+"0000";
                }
                else
                {
                    tx_string="0093010101"+Integer.toHexString(run_speed)+"0000";
                }
                Log.e(VEDIOSHOT_TAG, "trace "+ tx_string);
                if(!connect_status_bit)
                    return;
                mBluetoothLeService.txxx(tx_string);
                Log.e(VEDIOSHOT_TAG, tx_string);

            }
        });

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    String tx_string;
                    tx_string="0093010103ff0000";
                    if(!connect_status_bit)
                        return;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(VEDIOSHOT_TAG, tx_string);
                }else {
                    String tx_string;
                    tx_string="0093010103000000";
                    if(!connect_status_bit)
                        return;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(VEDIOSHOT_TAG, tx_string);
                    //Log.e("vedioshot","BBBBB");
                }
            }
        });

        mSwitch_danpai.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    String tx_string;
                    tx_string="0093010104ff0000";
                    if(!connect_status_bit)
                        return;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(VEDIOSHOT_TAG, tx_string);
                }else {
                    String tx_string;
                    tx_string="0093010104000000";
                    if(!connect_status_bit)
                        return;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(VEDIOSHOT_TAG, tx_string);
                    //Log.e("vedioshot","BBBBB");
                }
            }
        });

    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.e(VEDIOSHOT_TAG, "vedioshot onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(VEDIOSHOT_TAG, "Unable to initialize Bluetooth");
                finish();
            }
            {
                boolean result;
                result = mBluetoothLeService.connect(mDeviceAddress);
                Log.e(VEDIOSHOT_TAG, "vedioshot result "+result);
                if (result)
                {
                    mConnected = true;
                    connect_status_bit=true;
                    //timer.cancel();
                    Log.e(VEDIOSHOT_TAG, "vedioshot connected!");
                    mBluetoothLeService.txxx("0003010100000000");

                }
            }
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
            connect_intent = intent;
            Log.e(VEDIOSHOT_TAG, "vedioshot_123");
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                connect_status_bit=true;
                Log.e(VEDIOSHOT_TAG, "delayshot ACTION_GATT_CONNECTED");
                //delay(3000);
                //mBluetoothLeService.txxx("0093040100000000");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                mBluetoothLeService.disconnect();
                mBluetoothLeService = null;
                timer.cancel();
                timer=null;
                Intent intent1 = new Intent(VedioShot.this,
                        MainActivity.class);
                startActivity(intent1);
                connect_status_bit=false;
                Log.e(VEDIOSHOT_TAG, "MMM");
                //show_view(false);
                //invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.e(VEDIOSHOT_TAG, "OOO");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String str = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.e(VEDIOSHOT_TAG, "PPP"+ str);
                //03015001020304
                if (str.substring(0,4).equals("0301"))
                {
                    String tmp_str = str.substring(4,6);
                    Log.e(VEDIOSHOT_TAG, "seepd_string = "+tmp_str);
                    seekbar_speed.setProgress(Integer.valueOf(tmp_str,16)-1);
                    tmp_str = str.substring(6,8);
                    if (tmp_str.equals("00"))
                    {
                        Log.e(VEDIOSHOT_TAG, "ff");
                        mSwitch.setChecked(false);
                    }
                    else if(tmp_str.equals("FF"))
                    {
                        Log.e(VEDIOSHOT_TAG, "00");
                        mSwitch.setChecked(true);
                    }
                    else
                    {
                        Log.e(VEDIOSHOT_TAG, "fuck");
                    }
                    tmp_str = str.substring(8,10);
                    if (tmp_str.equals("00"))
                    {
                        Log.e(VEDIOSHOT_TAG, "ff");
                        mSwitch_danpai.setChecked(false);
                    }
                    else if(tmp_str.equals("FF"))
                    {
                        Log.e(VEDIOSHOT_TAG, "00");
                        mSwitch_danpai.setChecked(true);
                    }
                    else
                    {
                        Log.e(VEDIOSHOT_TAG, "fuck");
                    }

                    tmp_str = str.substring(10,12);
                    if (tmp_str.equals("FF"))
                    {
                        img_btn_left.setBackgroundResource(R.drawable.left_press);
                        img_btn_right.setBackgroundResource(R.drawable.right_release);
                    }
                    else if(tmp_str.equals("00"))
                    {
                        img_btn_left.setBackgroundResource(R.drawable.left_release);
                        img_btn_right.setBackgroundResource(R.drawable.right_press);
                    }
                    else
                    {
                        Log.e(VEDIOSHOT_TAG, "fuck");
                    }

                    tmp_str = str.substring(12,14);
                    if (tmp_str.equals("FF"))
                    {
                        mSwitch_danpai.setClickable(false);
                        img_btn_start.setBackgroundResource(R.drawable.stop);
                        start_press_flag = true;
                    }
                    else if(tmp_str.equals("00"))
                    {
                        mSwitch_danpai.setClickable(true);
                        img_btn_start.setBackgroundResource(R.drawable.start);
                        start_press_flag = false;
                    }
                    get_param_success = true;
                    timer.cancel();
                }
                else
                {
                    Log.e(VEDIOSHOT_TAG, "not equal to 030Bff");
                    get_param_success = false;
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
                //Log.e("vedioshot", "handleMessage");
                //tvShow.setText(Integer.toString(i++));
                //scanLeDevice(true);
                if (mBluetoothLeService != null) {
                    //Log.e("vedioshot", "mBluetoothLeService != null");
                    if( mConnected==false )
                    {
                        //updateConnectionState(R.string.connecting);
                        final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                        Log.e(VEDIOSHOT_TAG, "Connect request result=" + result);
                    }
                }
            }
            super.handleMessage(msg);
        };
    };
    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            //Log.e("vedioshot", "BBB");
            // ÐèÒª×öµÄÊÂ:·¢ËÍÏûÏ¢
            //Log.e("vedioshot", "timer task run");
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
            if (!get_param_success)
            {
                if (connect_status_bit)
                {
                    Log.e(VEDIOSHOT_TAG, "retry");
                    mBluetoothLeService.txxx("0003010100000000");
                }
            }
        }
    };

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(VEDIOSHOT_TAG, "state changed");
                //mConnectionState.setText(resourceId);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e(VEDIOSHOT_TAG, "CCC");
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
        Log.e(VEDIOSHOT_TAG, "DDD");
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
        Log.e(VEDIOSHOT_TAG, "EEE");
        if (id == R.id.vedio_shoot) {
            // Handle the camera action
        } else if (id == R.id.delay_shoot) {

            Intent intent1 = new Intent(VedioShot.this,
                    DelayShot.class);
            intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                    mDeviceName);
            intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                    mDeviceAddress);
            //timer.cancel();


            startActivity(intent1);

        } else if (id == R.id.abpoint) {
            Log.e(VEDIOSHOT_TAG, "abpoint");
            Intent intent1 = new Intent(VedioShot.this,
                    ABpoint.class);
            intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                    mDeviceName);
            intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                    mDeviceAddress);
            startActivity(intent1);
        }else if (id == R.id.support) {

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
        Intent intent=new Intent(this,VedioShot.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        Log.e(VEDIOSHOT_TAG, "FFF");
        if( gattServices.size()>0&&mBluetoothLeService.get_connected_status( gattServices )>=4 )
        {
            Log.e(VEDIOSHOT_TAG, "caonima");
            if(connect_status_bit)
            {
                Log.e(VEDIOSHOT_TAG, "connect  aaa");
                mConnected = true;
                //show_view( true );
                mBluetoothLeService.enable_JDY_ble(true);
                try {
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBluetoothLeService.enable_JDY_ble(true);
            }else{
                Log.e(VEDIOSHOT_TAG, "displayGattServices disconnect");
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
        Log.e(VEDIOSHOT_TAG, "GGG");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {

            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.e(VEDIOSHOT_TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        Log.e(VEDIOSHOT_TAG, "HHH");
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        Log.e(VEDIOSHOT_TAG, "III");
        super.onDestroy();
        mBluetoothLeService.disconnect();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        timer.cancel();
        timer=null;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        Log.e(VEDIOSHOT_TAG, "IntentFilter");
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


}
