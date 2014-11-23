package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by nadia on 17/11/14.
 * Activity that will display the results of the various sensors in a ListView
 * and update them through the bound service SensorDataService
 */
public class SensorDataActivity extends Activity {
    private static final String TAG = "SensorDataActivity";
    public static final String EXTRAS_DEVICE_NAME = "ExtrasDeviceName";
    public static final String EXTRAS_DEVICE_ADDRESS = "ExtrasDeviceAddress";

    private String mDeviceName;
    private String mDeviceAddress;
    private SensorDataService mSensorDataService;


    //interface for binding a service to the client activity
    //need to override onServiceConnected() and onServiceDisconnected() methods
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        //cast object of the SensorDataService into one of LocalBinder
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mSensorDataService = ((SensorDataService.LocalBinder) service).getService();
            // get bluetooth manager again (in the service)
            if (!(mSensorDataService.initialise())) {
                finish();
            }
            // establish bluetooth connection to the device again using the address
            //received in the Intent
            mSensorDataService.gattConnect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSensorDataService = null;
        }
    };


    //////////////////// Activity Lifecycle methods ////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate started");
        setContentView(R.layout.activity_sensor_data);

        //populate the HashMap of all services in the model class
        SensorDataModel.populateMap();

        // extract intent information from BluetoothActivity
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        Log.d(TAG, "Extracted device name: " + mDeviceName);
        Log.d(TAG, "Extracted device address: " + mDeviceAddress);

        //Bind to SensorDataService, thus starting it using intent
        Intent gattServiceIntent = new Intent(this, SensorDataService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        //Register to receive broadcasts
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                                                                new IntentFilter("Humidity_read"));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unbind from service when back button is pressed and activity is destroyed
        unbindService(mServiceConnection);
        //unregister from broadcast
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }


    //Handler for received events
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get extra data
            String result = intent.getStringExtra("RESULT");
            Log.d(TAG, "Received: " + result);
        }
    };
}
