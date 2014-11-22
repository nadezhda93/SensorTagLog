package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
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
 * Activity that will host a fragment to display the results of the various sensors on a new page
 * after the user has clicked on a device and has connected to GATT server
 */
public class SensorDataActivity extends Activity {
    private static final String TAG = "SensorDataActivity";
    public static final String EXTRAS_DEVICE_NAME = "ExtrasDeviceName";
    public static final String EXTRAS_DEVICE_ADDRESS = "ExtrasDeviceAddress";

    private String mDeviceName;
    private String mDeviceAddress;
    private SensorDataService mSensorDataService;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        // interface for binding a service to the activity
        // need to override onServiceConnected and onServiceDisconnected
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mSensorDataService = ((SensorDataService.LocalBinder) service).getService();
            // get bluetooth manager again (in the service)
            if (!mSensorDataService.initialise()) {
                finish();
            }
            // establish bluetooth connection to the device again
            //mSensorDataService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSensorDataService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate started");
        setContentView(R.layout.activity_sensor_data);
        // extract intent information
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        Log.d(TAG, "Extracted device name: " + mDeviceName);
        Log.d(TAG, "Extracted device address: " + mDeviceAddress);

        // bind service for Gatt server
        Intent gattServiceIntent = new Intent(this, SensorDataService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
