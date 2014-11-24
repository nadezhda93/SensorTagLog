package com.nadia.android.sensortaglog;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by nadia on 22/11/14.
 * A Bound Service which is used to connect to GattServer, set notifications for the SensorTag
 * sensors and send the readings to the SensorDataActivity.
 */
public class SensorDataService extends Service {
    private final static String TAG = "SensorDataService";

    private BluetoothAdapter sBluetoothAdapter;
    private BluetoothManager sBluetoothManager;
    private BluetoothGatt sConnectedGatt;

    private Handler enableHandler = new Handler();
    private Handler notificationHandler = new Handler();

    // required to extend Binder in order to make
    // a private bound service (BLE example Android dev)
    //http://developer.android.com/guide/components/bound-services.html
    public class LocalBinder extends Binder {
        SensorDataService getService() {
            return SensorDataService.this;
        }
    }
    
    //method to get an instance of BluetoothAdapter
    //using BluetoothManager
    //this function will be called inside ServiceConnection implementation in client activity
    public boolean initialise() {
        Log.d(TAG, "initialise() called");

        //initialise bluetooth adapter through BluetoothManager
        sBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        sBluetoothAdapter = sBluetoothManager.getAdapter();

        return true;
    }

    //method to connect to the gatt server on the device using the device address that
    //was passed in the Intent when the new activity was started
    public boolean gattConnect(final String address) {
        if (sBluetoothAdapter == null || address == null) {
            Log.d(TAG, "BluetoothAdapter not initialised or unspecified address.");
            return false;
        }

        final BluetoothDevice sDevice = sBluetoothAdapter.getRemoteDevice(address);

        // Set AutoConnect parameter to false to connect to device
        sConnectedGatt = sDevice.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        return true;
    }


    //Implementation of the GattCallback methods required in order to connect to gatt server
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.d(TAG, "Attempting to start service discovery:" + gatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "GATT services discovered");

                //enable Humidity sensor
                enableSensor(sConnectedGatt, (UUID)SensorDataModel.allServices.get("Humidity").get("HUMIDITY_SERVICE"),
                             (UUID)SensorDataModel.allServices.get("Humidity").get("HUMIDITY_CONFIG"), true);
                Log.d(TAG, "Humidity sensor enabled");
                //enable Accelerometer sensor after 1 sec

                enableHandler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        enableSensor(sConnectedGatt, (UUID) SensorDataModel.allServices.get("Accelerometer").get("ACCELEROMETER_SERVICE"),
                                (UUID) SensorDataModel.allServices.get("Accelerometer").get("ACCELEROMETER_CONFIG"), true);
                        Log.d(TAG, "Accelerometer sensor enabled");
                    }
                }, 1000);

            }
            else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
            if(status == BluetoothGatt.GATT_SUCCESS){
                Log.d(TAG, "Write operation successful");
                //enable notifications for Humidity sensor
                setNotification(sConnectedGatt,(UUID)SensorDataModel.allServices.get("Humidity").get("HUMIDITY_SERVICE"),
                             (UUID)SensorDataModel.allServices.get("Humidity").get("HUMIDITY_DATA"), true);

                //enable notifications for Accelerometer sensor after 1 s
                enableHandler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        setNotification(sConnectedGatt,(UUID)SensorDataModel.allServices.get("Accelerometer").get("ACCELEROMETER_SERVICE"),
                                (UUID)SensorDataModel.allServices.get("Accelerometer").get("ACCELEROMETER_DATA"), true);
                    }
                }, 1000);

            }
            else{
                Log.d(TAG, "Write operation returned status: " + status);
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            //DO SOMETHING WHEN SENSOR IS READ
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            //check for which service the characteristic is received and send appropriate broadcast
            if (characteristic.getUuid().equals(SensorDataModel.HUMIDITY_DATA)){
                //Log the value of the sensor and send to SensorDataActivity to display using an Intent
                float humidity = SensorDataModel.extractHumidityValues(characteristic);
                Log.d(TAG,"Humidity: "+ humidity);
                //Broadcast message to SensorDataActivity of the value
                sendMessage(humidity);
            }
            else if(characteristic.getUuid().equals(SensorDataModel.ACCELEROMETER_DATA)) {
                double[] result = SensorDataModel.extractAccelerometerValues(characteristic);
                Log.d(TAG, "Accelerometer: x = " + result[0] + " y = " + result[1] + " z = " + result[2]);
                sendMessage(result[0],result[1],result[2]);
            }

        }
    };



    //Turn on a sensor (SensorTag manual)
    //must be called after onServicesDiscovered finishes
    //DIFFERENT FOR GYROSCOPE
    //must wait for onCharacteristicWrite() callback method before issuing a new write operation
    private void enableSensor(BluetoothGatt bluetoothGatt, UUID serviceUuid, UUID configUuid, boolean enable){
        //needs to run after asynchronous discoverServices() finishes running
        //and onServicesDiscovered callback is called
        Log.d(TAG, "enableSensor started");
        BluetoothGattService sensorService = bluetoothGatt.getService(serviceUuid);
        BluetoothGattCharacteristic config = sensorService.getCharacteristic(configUuid);

        if(enable) {
            config.setValue(new byte[]{1});     //Different value for Gyroscope
            bluetoothGatt.writeCharacteristic(config);
            Log.d(TAG, "Sensor enabled");
        }
        else {
            config.setValue(new byte[]{0});     //Different value for Gyroscope
            bluetoothGatt.writeCharacteristic(config);
            Log.d(TAG, "Sensor disabled");
        }
    }

    //Setting notification for a service on the device locally and remotely
    //from SensorTag manual
    //must be called after enableSensor() write operation finishes
    private void setNotification(BluetoothGatt bluetoothGatt, UUID serviceUuid, UUID dataUuid, boolean enable){
        final UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        Log.d(TAG, "setNotification() started");

        BluetoothGattService sensorService = bluetoothGatt.getService(serviceUuid);
        BluetoothGattCharacteristic serviceDataCharacteristic = sensorService.getCharacteristic(dataUuid);

        if(enable) {
            bluetoothGatt.setCharacteristicNotification(serviceDataCharacteristic, true); //enabled locally
            BluetoothGattDescriptor configDescriptor = serviceDataCharacteristic.getDescriptor(CCC);
            configDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(configDescriptor); //enabled remotely
            Log.d(TAG, "Notifications set");
        }
        else {
            bluetoothGatt.setCharacteristicNotification(serviceDataCharacteristic, false); //disabled locally
            BluetoothGattDescriptor configDescriptor = serviceDataCharacteristic.getDescriptor(CCC);
            configDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(configDescriptor); //disabled remotely
            Log.d(TAG, "Notifications disabled");
        }
    }


    //method that broadcasts the result of the humidity sensor read using an intent to activities
    private void sendMessage(float result){
        Log.d(TAG, "Broadcasting message humidity...");
        Intent intent = new Intent("Humidity");
        //include result with the intent
        intent.putExtra("RESULT",  String.valueOf(result));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //method that broadcasts the result of the accelerometer sensor read using an intent to activities
    private void sendMessage(double x, double y, double z){
        Log.d(TAG, "Broadcasting message accelerometer...");
        Intent intent = new Intent("Accelerometer");
        //include results with intent
        intent.putExtra("RESULT x", String.valueOf(x));
        intent.putExtra("RESULT y", String.valueOf(y));
        intent.putExtra("RESULT z", String.valueOf(z));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    /////////////////////// Service Lifecycle methods   /////////////////////////

    //called when an activity binds to the service
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();

    }

    //called when an activity unbinds to the service - service is destroyed if nothing is bound
    @Override
    public boolean onUnbind(Intent intent) {
        // make sure all connections to Gatt device are closed
        sConnectedGatt.disconnect();

        return super.onUnbind(intent);
    }


}
