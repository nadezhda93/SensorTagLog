package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
//Bluetooth libraries
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.view.Window;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import android.util.Log;

public class BluetoothActivity extends Activity {


    //Device name constant
    private static final String DEVICE_NAME = "SensorTag";
    private static final String TAG = "BluetoothActivity";
    //1. IR temp UUIDs
    private static final UUID IR_SERVICE = UUID.fromString("f000aa00-0451-4000-b000-000000000000");
    private static final UUID IR_SERVICE_DATA = UUID.fromString("f000aa01-0451-4000-b000-000000000000");
    private static final UUID IR_SERVICE_CONFIG = UUID.fromString("f000aa02-0451-4000-b000-000000000000");

    //2. Accelerometer UUIDs
    private static final UUID ACCELEROMETER_SERVICE = UUID.fromString("f000aa10-0451-4000-b000-000000000000");
    private static final UUID ACCELEROMETER_SERVICE_DATA = UUID.fromString("f000aa11-0451-4000-b000-000000000000");
    private static final UUID ACCELEROMETER_SERVICE_CONFIG = UUID.fromString("f000aa12-0451-4000-b000-000000000000");

    //3. Gyroscope UUIDs
    private static final UUID GYRO_SERVICE = UUID.fromString("f000aa50-0451-4000-b000-000000000000");
    private static final UUID GYRO_SERVICE_DATA = UUID.fromString("f000aa51-0451-4000-b000-000000000000");
    private static final UUID GYRO_SERVICE_CONFIG = UUID.fromString("f000aa52-0451-4000-b000-000000000000");

    //4. Humidity Service UUID
    private static final UUID HUMIDITY_SERVICE = UUID.fromString("f000aa20-0451-4000-b000-000000000000");
    private static final UUID HUMIDITY_SERVICE_DATA = UUID.fromString("f000aa21-0451-4000-b000-000000000000");
    private static final UUID HUMIDITY_SERVICE_CONFIG = UUID.fromString("f000aa22-0451-4000-b000-000000000000");

    //5. Magnetometer UUIDs
    private static final UUID MAGNETOMETER_SERVICE = UUID.fromString("f000aa30-0451-4000-b000-000000000000");
    private static final UUID MAGNETOMETER_DATA = UUID.fromString("f000aa31-0451-4000-b000-000000000000");
    private static final UUID MAGNETOMETER_CONFIG = UUID.fromString("f000aa32-0451-4000-b000-000000000000");

    //6. Barometer UUIDs
    private static final UUID BAROMETER_SERVICE = UUID.fromString("f000aa40-0451-4000-b000-000000000000");
    private static final UUID BAROMETER_DATA = UUID.fromString("f000aa41-0451-4000-b000-000000000000");
    private static final UUID BAROMETER_CONFIG = UUID.fromString("f000aa42-0451-4000-b000-000000000000");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt    mConnectedGatt;

    private boolean mScanning;
    private Handler mHandler = new Handler();
    // Stops scanning after 5 seconds.
    private static final long SCAN_PERIOD = 5000;

    //Data Array in which to store found devices, accessed by ArrayAdapter
    private ArrayList<BluetoothDevice> mListOfDevices = new ArrayList<BluetoothDevice>();

    //initialise ArrayAdapter
    CustomListAdapter mDevicesAdapter;


    //find BLE devices using startLeScan() which uses a BluetoothAdapter.LeScanCallback which
    //is implemented

    //1. Device scan callback implementation - saves each device to the customAdapter which
    //will then be used to display a list of selectable items to connect to

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDevicesAdapter.add(device);
                            mDevicesAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };



    //2. startLeScan implementation
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            //start scrolling wheel
            setProgressBarIndeterminateVisibility(true);
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    //stop scrolling wheel
                    setProgressBarIndeterminateVisibility(false);
                }

            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }


    //Gatt service callback methods  Android DEV  ///////////////////////////////////////
    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    //Do something
                    /*String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        mConnectionState = STATE_CONNECTED;
                        broadcastUpdate(intentAction);
                        Log.i(TAG, "Connected to GATT server.");
                        Log.i(TAG, "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_DISCONNECTED;
                        mConnectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        broadcastUpdate(intentAction);
                    }*/
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    /*if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }*/
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {/*
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    }*/
                }
            };
    ////////////////////////////////////////////////////////////////////////////////////


    //private TextView mDevicesFound;

    private Button mScanButton;
    ////////////////onCreate() Lifecycle Activity Method ///////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate started");
        //set scrolling wheel for indeterminate progress
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_bluetooth);
        setProgressBarIndeterminate(true);



        //wire up scan button
        mScanButton = (Button)findViewById(R.id.action_scan);

        //initialise bluetooth adapter through bluetooth manager
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        //popup to ask user yo enable bluetooth if it isn't enabled
        if (!mBluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(),"Bluetooth turned on"
                    ,Toast.LENGTH_LONG).show();
        }



        ListView devicesListView = (ListView) findViewById(R.id.devices_found_list);
        //set the adapter to the listView in the layout using default android layout
        mDevicesAdapter = new CustomListAdapter(this, android.R.layout.simple_list_item_1, mListOfDevices);
        devicesListView.setAdapter(mDevicesAdapter);



        //Obtain a discovered device to connect with
        //Add any discovered devices to overflow menu
       // for(int i=0; i<mListOfDevices.size();i++){
         //   BluetoothDevice device= mListOfDevices.valueAt(i);
           // menu.add(0, mListOfDevices.keyAt(i), 0 , device.getName());
        //}

       // if (device != null) {
         //   mDevicesFound = (TextView) findViewById(R.id.device_name);

            //Display name of device on the screen
           // mDevicesFound.setText(device.getName());

            //make a connection to the device using LE specific
            //connectGatt() method, passing in a callback for Gatt events
            //mConnectedGatt = device.connectGatt(this, true, mGattCallback);
        //}

    }

    @Override
    protected void onPause(){
        super.onPause();
        scanLeDevice(false);
        mDevicesAdapter.clear();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        scanLeDevice(false);
        mDevicesAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
        return true;
    }

    //callback method to respond to clicks of buttons in the action bar or overflow menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
           case R.id.action_scan:
               //clear adapter of previous data
               mListOfDevices.clear();
               mDevicesAdapter.clear();
               //start device discovery when Scan is pressed
               scanLeDevice(true);
               return true;

            case R.id.action_settings:
                //do something when you press overflow and settings
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }



}



   /* private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mConnectedGatt;
    private SparseArray<BluetoothDevice> mListOfDevices;

    //display result in a text field
    private TextView mHumidity;

    private ProgressDialog mProgress;

    private Runnable mStopRunnable = () -> {stopScan();};
    private Runnable mStartRunnable = () -> {startScan();};

//BluetoothAdapter.LeScanCallback

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord){
        //validate name of each device that it is BLE
        if(DEVICE_NAME.equals(device.getName())){
            mListOfDevices.put(device.hashCode(),device);
            //update overflow menu
            invalidateOptionsMenu();
        }

    }

    private void startScan(){
        mBluetoothAdapter.startLeScan(this);
        setProgressBarIndeterminateVisibility(true);
        mHandler.postDelayed(mStopRunnable, 2500);

    }

    private void stopScan(){
        mBluetoothAdapter.stopLeScan(this);
        setProgressBarIndeterminateVisibility(false);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_bluetooth);
        setProgressBarIndeterminate(true);

        //Wire up text view field to display result
        mHumidity = (TextView)findViewById(R.id.humidity_value);


        //Initialise Bluetooth adapter
        final BluetoothManager bluetoothManager =
                        (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();

        mListOfDevices = new SparseArray<BluetoothDevice>();

        //set up progress dialog while a connection is taking place
        mProgress = new ProgressDialog(this);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);


    }


    @Override
    protected void onResume(){
        super.onResume();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth disabled
            //from child activity to parent activity prompt to enable bluetooth
            //ask user to switch on bluetooth
            //do nothing for the time being
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        //Check for Bluetooth LE support
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, "No LE Support", Toast.LENGTH_SHORT).show();
            finish();
        }


    }

    @Override
    protected void onPause(){
        super.onPause();
        //dialog missing
        mProgress.dismiss();
        //if home button is pressed, stop scanning
        mHandler.removeCallbacks(mStopRunnable);
        mHandler.removeCallbacks(mStartRunnable);
        mBluetoothAdapter.stopLeScan(this);


    }

    @Override
    protected void onStop(){
        super.onStop();
        //Disconnect from any connection
        if(mConnectedGatt != null){
            mConnectedGatt.disconnect();
            mConnectedGatt = null;
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //add scan option to menu
        getMenuInflater().inflate(R.menu.menu_bluetooth, menu);

        //Add any discovered devices to overflow menu
        for(int i=0; i<mListOfDevices.size();i++){
            BluetoothDevice device = mListOfDevices.valueAt(i);
            menu.add(0, mListOfDevices.keyAt(i), 0 , device.getName());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_scan:
                mListOfDevices.clear();
                startScan();
                return true;
            default:
                //Obtain a discovered device to connect with
                BluetoothDevice device = mListOfDevices.get(item.getItemId());
                //make a connection to the device using LE specific
                //connectGatt() method, passing in a callback for Gatt events
                mConnectedGatt = device.connectGatt(this,true,mGattCallback);
                //Display progress UI
                mHandler.sendMessage(Message.obtain(null,MSG_PROGRESS,"Connecting to " +device.getName()+"..." );

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
           // return true;
        //}

        return super.onOptionsItemSelected(item);
    }
}
*/