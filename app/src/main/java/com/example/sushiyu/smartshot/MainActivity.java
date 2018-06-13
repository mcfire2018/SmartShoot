package com.example.sushiyu.smartshot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String MAINACTIVITY_TAG = "mcfire_main";
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 100000;

    private DeviceListAdapter mDevListAdapter;

    private List<BluetoothDevice> bt_device_test;

    BluetoothDevice device_select;
    ToggleButton tb_on_off;
    TextView btn_searchDev;
    Button btn_aboutUs;
    ListView lv_bleList;
    private DrawerLayout drawerLayout;
    View view1;
    LayoutInflater inflater1;
    ActionBarDrawerToggle toggle;
    public BluetoothLeService mBluetoothLeService;
    public boolean mConnected = false;
    private Intent connect_intent;
    public  boolean connect_status_bit=false;
    private int wait_receive_mcu_msg_to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String language = getResources().getConfiguration().locale.getLanguage();
        Log.e(MAINACTIVITY_TAG, "language == "+language);
        shiftLanguage(language);
        setContentView(R.layout.activity_vedio_shot);


        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!mBluetoothAdapter.isEnabled())
        {
            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(mIntent, 1);
        }

        bt_device_test = new ArrayList<BluetoothDevice>();
        //bt_device_test = mBluetoothAdapter.getBondedDevices();

        lv_bleList = (ListView) findViewById(R.id.lv_bleList);


        mDevListAdapter = new DeviceListAdapter();

        lv_bleList.setAdapter(mDevListAdapter);

        lv_bleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (mDevListAdapter.getCount() > 0) {
                    Log.e(MAINACTIVITY_TAG, "i am a bus driver");

                    lv_bleList.setEnabled(false);
                    BluetoothDevice device_select = mDevListAdapter.getItem(position);
                    if (device_select == null) {
                        Log.e(MAINACTIVITY_TAG, "device == null");
                        return;
                    }
                    /*
                    Log.e(MAINACTIVITY_TAG, device_select.getName());
                    Log.e(MAINACTIVITY_TAG, device_select.getAddress());
                    Intent intent1 = new Intent(MainActivity.this,
                            VedioShot.class);

                    intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                            device_select.getName());
                    intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                            device_select.getAddress());
                    if (mScanning) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mScanning = false;
                    }
                    Log.e(MAINACTIVITY_TAG, device_select.getName());
                    Log.e(MAINACTIVITY_TAG, device_select.getAddress());
                    startActivity(intent1);
                    */
                    mDeviceAddress = device_select.getAddress();
                    mDeviceName = device_select.getName();
                    mHandler = new Handler();
                    timer.schedule(task, 10, 100);
                    boolean sg;
                    Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
                    sg = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                    registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                    //updateConnectionState(R.string.connecting);
                }
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



    }

    Timer timer = new Timer();

    Timer timer_wait_mcu = new Timer();

    public void delay(int ms){
        try {
            Thread.currentThread();
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            if (msg.what == 1) {
                //Log.e(MAINACTIVITY_TAG, "handleMessage");
                //tvShow.setText(Integer.toString(i++));
                //scanLeDevice(true);
                if (mBluetoothLeService != null) {
                    //Log.e(MAINACTIVITY_TAG, "mBluetoothLeService != null");
                    if( mConnected==false )
                    {
                        //updateConnectionState(R.string.connecting);
                        final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                        Log.e(MAINACTIVITY_TAG, "Connect request result=" + result);
                    }
                }
            }
            super.handleMessage(msg);
        };
    };
    TimerTask task_wait_mcu = new TimerTask() {
        @Override
        public void run() {
            wait_receive_mcu_msg_to--;
            Log.e(MAINACTIVITY_TAG, "cnt = "+wait_receive_mcu_msg_to);
            if (wait_receive_mcu_msg_to == 0)
            {
                Log.e(MAINACTIVITY_TAG, "cnt = "+wait_receive_mcu_msg_to);
                timer.cancel();
                timer_wait_mcu.cancel();

                Intent intent1 = new Intent(MainActivity.this,
                        VedioShot.class);
                intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                        mDeviceName);
                intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                        mDeviceAddress);
                startActivity(intent1);
            }
        }
    };

    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            //Log.e(MAINACTIVITY_TAG", "BBB");
            // ÐèÒª×öµÄÊÂ:·¢ËÍÏûÏ¢
            //Log.e(MAINACTIVITY_TAG, "timer task run");
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.e(MAINACTIVITY_TAG, "main onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(MAINACTIVITY_TAG, "Unable to initialize Bluetooth");
                finish();
            }
            {
                boolean result;
                result = mBluetoothLeService.connect(mDeviceAddress);
                Log.e(MAINACTIVITY_TAG, "main result "+result);
                if (result)
                {
                    mConnected = true;
                    connect_status_bit=true;

                    //timer.cancel();
                    //delay(1000);

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
            Log.e(MAINACTIVITY_TAG, "KKK");
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                connect_status_bit=true;
                delay(3000);
                Log.e(MAINACTIVITY_TAG, "tx 0093040100000000");
                timer_wait_mcu.schedule(task_wait_mcu, 1, 500);
                wait_receive_mcu_msg_to = 10;
                mBluetoothLeService.txxx("0093040100000000");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                mBluetoothLeService.disconnect();
                mBluetoothLeService = null;
                timer.cancel();
                timer=null;
                //updateConnectionState(R.string.disconnected);
                Intent intent1 = new Intent(MainActivity.this,
                        MainActivity.class);
                startActivity(intent1);
                connect_status_bit=false;
                Log.e(MAINACTIVITY_TAG, "MMM");
                //show_view(false);
                //invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.e(MAINACTIVITY_TAG, "OOO");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String str = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.e(MAINACTIVITY_TAG, "PPP"+ str);
                if (str.equals("030BFF") )
                {
                    Log.e(MAINACTIVITY_TAG, "ready to enter AB Point Setting");
                    timer.cancel();
                    timer_wait_mcu.cancel();

                    Intent intent1 = new Intent(MainActivity.this,
                            ABpoint.class);
                    intent1.putExtra(MainActivity.EXTRAS_DEVICE_NAME,
                            mDeviceName);
                    intent1.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS,
                            mDeviceAddress);
                    startActivity(intent1);
                }
                else if (str.substring(0,6).equals("030B00") )
                {
                    Log.e(MAINACTIVITY_TAG, "030B00");
                    timer.cancel();
                    timer_wait_mcu.cancel();

                    if (str.substring(6,10).equals("030A"))
                    {
                        String shot_maxtime = str.substring(12,14) + str.substring(10,12);
                        DelayShot.max_shot_times_abpoint = Integer.valueOf(shot_maxtime,16);

                        Log.e(MAINACTIVITY_TAG, "max shot time"+ DelayShot.max_shot_times);
                    }

                    Intent intent1 = new Intent(MainActivity.this,
                            VedioShot.class);
                    intent1.putExtra(MainActivity.EXTRAS_DEVICE_NAME,
                            mDeviceName);
                    intent1.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS,
                            mDeviceAddress);
                    startActivity(intent1);
                }

                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return false;
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

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        Log.e(MAINACTIVITY_TAG, "FFF");
        if( gattServices.size()>0&&mBluetoothLeService.get_connected_status( gattServices )>=4 )
        {
            Log.e(MAINACTIVITY_TAG, "caonima");
            if(connect_status_bit)
            {
                Log.e(MAINACTIVITY_TAG, "connect  aaa");
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
                //updateConnectionState(R.string.connected);
            }else{
                Log.e(MAINACTIVITY_TAG, "displayGattServices disconnect");
            }
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
            timer.cancel();
            timer_wait_mcu.cancel();
            Intent intent1 = new Intent(MainActivity.this, VedioShot.class);
            startActivity(intent1);

        } else if (id == R.id.delay_shoot) {
            timer.cancel();
            timer_wait_mcu.cancel();
            Intent intent2 = new Intent(MainActivity.this, DelayShot.class);
            startActivity(intent2);
        } else if (id == R.id.abpoint) {
            timer.cancel();
            timer_wait_mcu.cancel();
            Intent intent1 = new Intent(MainActivity.this, ABpoint.class);
            startActivity(intent1);

        } else if (id == R.id.software_upgrade) {

            UpdateManager manager = new UpdateManager(MainActivity.this);
            // 检查软件更新
            manager.checkUpdate("https://www.baidu.com");
            /*try{
                Update.software_update();
            }
            catch(Exception e){
                System.out.println("Wrong!");
            }*/


        } else if (id == R.id.support) {
            //UpdateManager manager = new UpdateManager(MainActivity.this);
            // 检查软件更新
            //manager.checkUpdate("https://www.sina.com.cn/");
            //manager.checkUpdate("https://block.sinacloud.com/#/detail/files/version.xml");
            //manager.checkUpdate("https://github.com/mcfire2018/SmartShoot/blob/master/version.xml");
            HttpDownloader downloader ;
            downloader = new HttpDownloader();
            String urlStr = "https://www.baidu.com";
            downloader.download(urlStr);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void shiftLanguage(String sta){

        if(sta.equals("zh")){
            Locale.setDefault(Locale.CHINESE);
            Configuration config = getBaseContext().getResources().getConfiguration();
            config.locale = Locale.CHINESE;
            getBaseContext().getResources().updateConfiguration(config
                    , getBaseContext().getResources().getDisplayMetrics());
            //refreshSelf();
        }else{
            Locale.setDefault(Locale.US);
            Configuration config = getBaseContext().getResources().getConfiguration();
            config.locale = Locale.US;
            getBaseContext().getResources().updateConfiguration(config
                    , getBaseContext().getResources().getDisplayMetrics());
            //refreshSelf();
        }
    }
    //refresh self
    public void refreshSelf(){
        Intent intent=new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 0:
                break;
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDevListAdapter.addDevice(device);
                    mDevListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    protected void onResume() {//打开APP时扫描设备
        super.onResume();
        scanLeDevice(true);
        lv_bleList.setEnabled(true);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {//停止扫描
        super.onPause();
        scanLeDevice(false);
        unregisterReceiver(mGattUpdateReceiver);
    }




    class DeviceListAdapter extends BaseAdapter {

        private List<BluetoothDevice> mBleArray;
        private ViewHolder viewHolder;

        public DeviceListAdapter() {
            mBleArray = new ArrayList<BluetoothDevice>();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mBleArray.contains(device)) {
                mBleArray.add(device);
            }
        }
        public void clear(){
            mBleArray.clear();
        }

        @Override
        public int getCount() {
            return mBleArray.size();
        }

        @Override
        public BluetoothDevice getItem(int position) {
            return mBleArray.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(
                        R.layout.list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.tv_devName = (TextView) convertView
                        .findViewById(R.id.device_name);
                viewHolder.tv_devAddress = (TextView) convertView
                        .findViewById(R.id.device_address);
                convertView.setTag(viewHolder);
            } else {
                convertView.getTag();
            }

            // add-Parameters
            BluetoothDevice device = mBleArray.get(position);
            String devName = device.getName();
            if (devName != null && devName.length() > 0) {
                viewHolder.tv_devName.setText(devName);
            } else {
                viewHolder.tv_devName.setText("unknow-device");
            }
            viewHolder.tv_devAddress.setText(device.getAddress());

            return convertView;
        }

    }

    class ViewHolder {
        TextView tv_devName, tv_devAddress;
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        Log.e(MAINACTIVITY_TAG, "IntentFilter");
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public  String tmp_bin2hex(String bin) {
        char[] digital = "0123456789ABCDEF".toCharArray();
        StringBuffer sb = new StringBuffer("");
        byte[] bs = bin.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(digital[bit]);
            bit = bs[i] & 0x0f;
            sb.append(digital[bit]);
        }
        return sb.toString();
    }
}
