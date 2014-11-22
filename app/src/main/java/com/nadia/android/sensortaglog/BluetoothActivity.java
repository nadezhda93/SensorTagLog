package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
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
import java.util.List;
import java.util.UUID;


public class BluetoothActivity extends Activity {
    //Device name constant
    private static final String DEVICE_NAME = "SensorTag";
    private static final String TAG = "BluetoothActivity";

    private Button mScanButton;
    private ListView mDevicesListView;

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

    //3. gattConnect method to connect to Gatt services on the device that was clicked
    //in the ListView
    private void gattConnect(BluetoothDevice device) {
        //make a connection to the device using LE specific connectGatt() method,
        //passing in a callback for Gatt events
        mConnectedGatt = device.connectGatt(this, true, mGattCallback);
        //start scrolling wheel
        setProgressBarIndeterminateVisibility(true);
        mConnectedGatt.discoverServices();
    }


    //4. Gatt service callback methods
    //Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                //method when to connection to GATT server changes
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.d(TAG, "Connected to GATT server.");

                        //Activity.runOnUiThread used to update user interface inside a callback
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //stop scrolling wheel
                                setProgressBarIndeterminateVisibility(false);
                                //show toast to user that they have connected
                                Toast.makeText(BluetoothActivity.this, "Connected to device", Toast.LENGTH_SHORT).show();

                            }
                        });
                        //discover services on device - asynchronous, when successful, calls onServicesDiscovered below
                        Log.d(TAG, "Attempting to start service discovery:" + gatt.discoverServices());
                    }

                    else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.d(TAG, "Disconnected from GATT server.");
                        //Activity.runOnUiThread used to update user interface inside a callback
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //show toast to user that they have disconnected
                                Toast.makeText(BluetoothActivity.this, "Disconnected from device", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                //method when new services are discovered after calling BluetoothGatt.discoverServices();
                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d(TAG, "Gatt services discovered");
                        BluetoothGattService humidity = gatt.getService(HUMIDITY_SERVICE);
                        BluetoothGattCharacteristic humidityData = humidity.getCharacteristic(HUMIDITY_SERVICE_DATA);
                        Log.d(TAG, String.valueOf(SensorTagDataConvert.extractHumidityValues(humidityData)));
                    }
                    else {
                        Log.d(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                // Result of a characteristic read operation
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    //DO SOMETHING WHEN SENSOR IS READ
                }

                //Result of remote characteristic notification
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic){
                    //DO SOMETHING WHEN NOTIFICATION COMES

                }
            };

    //5. Get a list of all the services and pass it to the next activity
    //called inside BluetoothGattCallback because it takes time for
    //services to be discovered which is asynchronous
    private void listGattServices(){
        Log.d(TAG, "listGattServices started");

        List<BluetoothGattService> listOfServices = mConnectedGatt.getServices();

        List<BluetoothGattCharacteristic> characteristics = listOfServices.get(0).getCharacteristics();

        List<BluetoothGattDescriptor> descriptors = characteristics.get(0).getDescriptors();

        if(listOfServices.isEmpty()){
            Log.d(TAG, "ListOfServices empty");
        }

        else {
            Log.d(TAG, "ListOfServices NOT empty");

            for (int i = 0 ; i<characteristics.size(); i++) {
                //get a list of all characteristics
                Log.d(TAG, "CHARACTERISTIC: " + characteristics.get(i).toString());
                //Log.d(TAG, "Service: " + listOfServices.get(i).getUuid().toString());
                //Log.d(TAG, "Characteristic: "+ c.getService());
            }

            for (int j = 0; j< descriptors.size(); j++){
                Log.d (TAG, "DESCRIPTOR: "+ descriptors.get(j).toString());
            }
        }

        //Start the new activity to show list of services SensorDataActivity
        //by making a new Intent and sending on mConnectedGatt
        //Intent newActivity = new Intent(BluetoothActivity.this, SensorDataActivity.class);
        //newActivity.putExtra("mConnectedGatt", listOfServices);
        //startActivity(newActivity);
    }


    ////////////////////////////////////////////////////////////////////////////////////



    ////////////////onCreate() Lifecycle Activity Method ///////////////////////////////
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
            Toast.makeText(getApplicationContext(),"Bluetooth turned on"
                    ,Toast.LENGTH_LONG).show();
        }


        //wire up ListView widget by resource ID
        mDevicesListView = (ListView) findViewById(R.id.devices_found_list);
        //set the adapter to the listView in the layout using default android layout
        mDevicesAdapter = new CustomListAdapter(this, android.R.layout.simple_list_item_1, mListOfDevices);
        mDevicesListView.setAdapter(mDevicesAdapter);

        //respond to clicks in the list view, call method gattConnect() on clicked item
        mDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //when clicked, log that it was clicked, connect to gatt server
                BluetoothDevice clicked_device = mDevicesAdapter.getItem(position);

                Log.d(TAG, clicked_device.getName()+ " was clicked");
                //connect to gatt on this device
                gattConnect(clicked_device);
            }
        });

    }

    @Override
    protected void onPause(){
        super.onPause();
        scanLeDevice(false);
        //empty list of found devices
        mDevicesAdapter.clear();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        scanLeDevice(false);
        //empty list of found devices
        mDevicesAdapter.clear();
        //close any GATT connection to server
        mConnectedGatt.disconnect();
        mConnectedGatt.close();
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
