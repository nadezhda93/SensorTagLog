package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.os.Handler;
import java.util.ArrayList;

public class BluetoothActivity extends Activity {
    private static final String TAG = "BluetoothActivity";

    private Button mScanButton;
    private ListView mDevicesListView;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private boolean mScanning;
    private Handler mHandler = new Handler();
    // Stops scanning after 5 seconds.
    private static final long SCAN_PERIOD = 5000;
    //Data Array in which to store found devices, accessed by ArrayAdapter
    private ArrayList<BluetoothDevice> mListOfDevices = new ArrayList<BluetoothDevice>();
    //initialise ArrayAdapter
    private CustomListAdapter mDevicesAdapter;

    //1. Device scan callback implementation - saves each device to the CustomListAdapter which
    //will then be used to display a list of selectable items to connect to
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    //Activity.runOnUiThread used to update user interface inside a callback
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDevicesAdapter.add(device);
                            mDevicesAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    //2. startLeScan implementation - find LE devices only scan for SCAN_PERIOD milliseconds
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

    //////////////////// Activity Lifecycle methods ////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate started");
        //set scrolling wheel for indeterminate progress
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_bluetooth);
        setProgressBarIndeterminate(true);

        //wire up scan button in actionbar
        mScanButton = (Button)findViewById(R.id.action_scan);

        //initialise bluetooth adapter through BluetoothManager
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        //popup to ask user to enable bluetooth if it isn't enabled - OS driven
        if (!mBluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }

        //wire up ListView widget by resource ID
        mDevicesListView = (ListView) findViewById(R.id.devices_found_list);
        //set the adapter to the listView in the layout using default android list layout
        mDevicesAdapter = new CustomListAdapter(this,
                            android.R.layout.simple_list_item_1, mListOfDevices);
        mDevicesListView.setAdapter(mDevicesAdapter);

        //respond to clicks in the list view, call method gattConnect() on clicked item
        mDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //when clicked, log that it was clicked, stop scanning for devices
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                BluetoothDevice clicked_device = mDevicesAdapter.getItem(position);
                // open new activity using an intent and send in strings on device name and address
                //they will then be used to connect to gattserver in the activity/service
                //open only if device is SensorTag

                if(clicked_device.getName() != null) {
                    if (clicked_device.getName().equals("SensorTag")) {
                        Log.d(TAG, clicked_device.getName() + " was clicked");
                        final Intent intent = new Intent(BluetoothActivity.this,
                                                   SensorDataActivity.class);
                        intent.putExtra(SensorDataActivity.EXTRAS_DEVICE_NAME,
                                                   clicked_device.getName());
                        intent.putExtra(SensorDataActivity.EXTRAS_DEVICE_ADDRESS,
                                                   clicked_device.getAddress());
                        //open new page/activity to display the available services
                        //onStop() is called here
                        startActivity(intent);
                    }
                }
                else {
                    //Display a message requiring the device to be a SensorTag
                    Toast.makeText(BluetoothActivity.this, "Device must be a SensorTag.",
                                                     Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //called when a new semi-transparent activity partially obscures the current activity
    @Override
    protected void onPause(){
        super.onPause();
        scanLeDevice(false);

    }

    //called when another app is switched to (hold Home button>select app)
    //when your current activity starts a new activity (starting SensorDataActivity)
    //when you receive a phonecall
    @Override
    protected void onStop(){
        super.onStop();
        scanLeDevice(false);
    }

    //called when second activity is destroyed and this activity is back on screen
    //should automatically be in the same state as before
    @Override
    protected void onStart(){
        super.onStop();
    }

    //when back button is pressed and activity is destroyed
    @Override
    protected void onDestroy(){
        super.onDestroy();
        scanLeDevice(false);
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
