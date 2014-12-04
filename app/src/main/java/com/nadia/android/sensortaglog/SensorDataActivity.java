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

    public static final String ACCELEROMETER_INTENT_FILTER = "com.nadia.android.sensortaglog.Accelerometer";
    public static final String GYROSCOPE_INTENT_FILTER = "com.nadia.android.sensortaglog.Gyroscope";
    public static final String MAGNETOMETER_INTENT_FILTER = "com.nadia.android.sensortaglog.Magnetometer";

    private String mDeviceName;
    private String mDeviceAddress;
    private SensorDataService mSensorDataService;

    private TextView mAccelerometerValue;
    private TextView mGyroscopeValue;
    private TextView mMagnetometerValue;

    private SensorDataSQLiteHelper db = new SensorDataSQLiteHelper(this);

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
                                                                new IntentFilter(MAGNETOMETER_INTENT_FILTER));
        //Register to receive broadcasts for Accelerometer
        LocalBroadcastManager.getInstance(this).registerReceiver(mAccMessageReceiver,
                                                                new IntentFilter(ACCELEROMETER_INTENT_FILTER));
        //Register to receive broadcasts for Gyro
        LocalBroadcastManager.getInstance(this).registerReceiver(mGyroMessageReceiver,
                                                                new IntentFilter(GYROSCOPE_INTENT_FILTER));

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
            float resultX = intent.getFloatExtra("RESULT x", 0);
            float resultY = intent.getFloatExtra("RESULT y", 0);
            float resultZ = intent.getFloatExtra("RESULT z", 0);

            //convert to strings, two dec places
            String result_x = String.format("%.2f", resultX);
            String result_y = String.format("%.2f", resultY);
            String result_z = String.format("%.2f", resultZ);
            Log.d(TAG, "Received Magnetometer: x = " + result_x + " y = " + result_y + " z = "+result_z);
            mMagnetometerValue.setText("x = " + result_x + " y = " +
                                                result_y + " z = " +
                                                result_z + " uT");

            //add values to table
            db.addToDatabaseTable(resultX,resultY, resultZ, MAGNETOMETER_INTENT_FILTER);
        }
    };

    private BroadcastReceiver mAccMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get extra data as doubles - cast to floats
            double resultX = intent.getDoubleExtra("RESULT x", 0.00);
            double resultY = intent.getDoubleExtra("RESULT y", 0.00);
            double resultZ = intent.getDoubleExtra("RESULT z", 0.00);

            //convert to strings two decimal places
            String result_x = String.format("%.2f", resultX);
            String result_y = String.format("%.2f", resultY);
            String result_z = String.format("%.2f", resultZ);
            Log.d(TAG, "Received Accelerometer: x = " + result_x + " y = " + result_y + " z = "+result_z);

            mAccelerometerValue.setText("x = " + result_x + " y = " +
                                                 result_y + " z = " +
                                                 result_z + " g");      //display as strings


            //add values to table
            db.addToDatabaseTable((float)resultX,(float)resultY,(float)resultZ, ACCELEROMETER_INTENT_FILTER);
        }
    };

    private BroadcastReceiver mGyroMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get extra data
            float resultX = intent.getFloatExtra("RESULT x", 0);
            float resultY = intent.getFloatExtra("RESULT y", 0);
            float resultZ = intent.getFloatExtra("RESULT z", 0);

            //convert to strings, two dec places
            String result_x = String.format("%.2f", resultX);
            String result_y = String.format("%.2f", resultY);
            String result_z = String.format("%.2f", resultZ);

            Log.d(TAG, "Received Gyroscope: x = " + result_x + " y = " + result_y + " z = "+result_z);

            mGyroscopeValue.setText("x = " + result_x + " y = " +
                    result_y + " z = " +
                    result_z + " deg");

            //add values to table
            db.addToDatabaseTable(resultX,resultY, resultZ, GYROSCOPE_INTENT_FILTER);
        }
    };
}
