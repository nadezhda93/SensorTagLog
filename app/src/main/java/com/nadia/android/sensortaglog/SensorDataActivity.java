package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
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

    HashMap<UUID, String> mapServices = new HashMap<UUID, String>();

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




    private static final String TAG = "SensorDataActivity";
    private ListView mSensorsListView;
    private ArrayAdapter<String> mSensorsAdapter;
    private ArrayList<String> mListOfSensors = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate started");
        setContentView(R.layout.activity_sensor_data);

        //wire up widgets
        mSensorsListView = (ListView) findViewById(R.id.sensors_found_list);

        //set the adapter to the listView in the layout using default android layout
        mSensorsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mListOfSensors);
        mSensorsListView.setAdapter(mSensorsAdapter);


        //respond to clicks in the list view
        mSensorsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //DO SOMETHING ON CLICK

            }
        });

        mapServices.put(IR_SERVICE, "Infrared Temperature");
        mapServices.put(HUMIDITY_SERVICE, "Humidity");
        mapServices.put(BAROMETER_SERVICE, "Barometer");

        mListOfSensors.add(mapServices.get(IR_SERVICE));
        mListOfSensors.add(mapServices.get(HUMIDITY_SERVICE));
        mListOfSensors.add(mapServices.get(BAROMETER_SERVICE));

        mSensorsAdapter.notifyDataSetChanged();


    }
}
