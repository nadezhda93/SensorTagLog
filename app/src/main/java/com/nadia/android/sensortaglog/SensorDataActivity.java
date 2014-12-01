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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by nadia on 17/11/14.
 * Activity that will display the results of the various sensors in TextViews
 * and update them through the bound service SensorDataService using a BroadcastReceiver
 */
public class SensorDataActivity extends Activity {
    private static final String TAG = "SensorDataActivity";
    public static final String EXTRAS_DEVICE_NAME = "ExtrasDeviceName";
    public static final String EXTRAS_DEVICE_ADDRESS = "ExtrasDeviceAddress";

    private String mDeviceName;
    private String mDeviceAddress;
    private SensorDataService mSensorDataService;

    private TextView mAccelerometerValue;
    private TextView mGyroscopeValue;
    private TextView mMagnetometerValue;


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

        //populate the HashMap of UUIDS of all services in the SensorDataModel class
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

        //wire TextView widgets
        mAccelerometerValue = (TextView)findViewById(R.id.accelerometer_value);
        mMagnetometerValue = (TextView)findViewById(R.id.mag_value);
        mGyroscopeValue = (TextView)findViewById(R.id.gyro_value);

        //Register to receive broadcasts for Magnetometer
        LocalBroadcastManager.getInstance(this).registerReceiver(mMagnetometerMessageReceiver,
                                                                new IntentFilter("Magnetometer"));
        //Register to receive broadcasts for Accelerometer
        LocalBroadcastManager.getInstance(this).registerReceiver(mAccMessageReceiver,
                                                                new IntentFilter("Accelerometer"));
        //Register to receive broadcasts for Gyro
        LocalBroadcastManager.getInstance(this).registerReceiver(mGyroMessageReceiver,
                                                                new IntentFilter("Gyroscope"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unbind from service when back button is pressed and activity is destroyed
        unbindService(mServiceConnection);
        //unregister from broadcast
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMagnetometerMessageReceiver);
    }


    //Handler for received events
    private BroadcastReceiver mMagnetometerMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get extra data
            String result_x = intent.getStringExtra("RESULT x");
            String result_y = intent.getStringExtra("RESULT y");
            String result_z = intent.getStringExtra("RESULT z");
            Log.d(TAG, "Received Magnetometer: x = " + result_x + " y = " + result_y + " z = "+result_z);
            mMagnetometerValue.setText("x = " + result_x + " y = " +
                                                result_y + " z = " +
                                                result_z + " uT");
        }
    };

    private BroadcastReceiver mAccMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get extra data
            String result_x = intent.getStringExtra("RESULT x");
            String result_y = intent.getStringExtra("RESULT y");
            String result_z = intent.getStringExtra("RESULT z");
            Log.d(TAG, "Received Accelerometer: x = " + result_x + " y = " + result_y + " z = "+result_z);

            mAccelerometerValue.setText("x = " + result_x + " y = " +
                                                 result_y + " z = " +
                                                 result_z + " g");
        }
    };

    private BroadcastReceiver mGyroMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get extra data
            String result_x = intent.getStringExtra("RESULT x");
            String result_y = intent.getStringExtra("RESULT y");
            String result_z = intent.getStringExtra("RESULT z");
            Log.d(TAG, "Received Gyroscope: x = " + result_x + " y = " + result_y + " z = "+result_z);

            mGyroscopeValue.setText("x = " + result_x + " y = " +
                    result_y + " z = " +
                    result_z + " deg");
        }
    };
}
