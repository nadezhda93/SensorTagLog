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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.util.UUID;

/**
 * Created by nadia on 22/11/14.
 * A Bound Service which is used to connect to GattServer, set notifications for the SensorTag
 * sensors and send the readings to the SensorDataActivity using BroadcastReceiver.
 * Based on BluetoothLeGatt service by Android dev
 */
public class SensorDataService extends Service {
    private final static String TAG = "SensorDataService";

    private BluetoothAdapter sBluetoothAdapter;
    private BluetoothManager sBluetoothManager;
    private BluetoothGatt sConnectedGatt;

    private Handler enableHandler = new Handler();

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

            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server");
            }
        }
        //when discovery of services and characteristics completes
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "GATT services discovered");
                //change period for accelerometer
                changePeriod(sConnectedGatt, (UUID)SensorDataModel.allServices.get("Accelerometer").get("ACCELEROMETER_SERVICE"),
                        SensorDataModel.ACCELEROMETER_PERIOD);

                enableHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //change period for gyroscope
                        changePeriod(sConnectedGatt, (UUID)SensorDataModel.allServices.get("Gyroscope").get("GYRO_SERVICE"),
                                SensorDataModel.GYRO_PERIOD);
                    }
                }, 500);

                enableHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //change period for magnetometer
                        changePeriod(sConnectedGatt, (UUID)SensorDataModel.allServices.get("Magnetometer").get("MAGNETOMETER_SERVICE"),
                                SensorDataModel.MAGNETOMETER_PERIOD);
                    }
                }, 1000);

                //ENABLE SENSORS
                enableHandler.postDelayed(new Runnable() {
                    @Override
                   public void run() {
                //enable Accelerometer sensor
                        enableSensor(sConnectedGatt, (UUID) SensorDataModel.allServices.get("Accelerometer").get("ACCELEROMETER_SERVICE"),
                                (UUID) SensorDataModel.allServices.get("Accelerometer").get("ACCELEROMETER_CONFIG"), true);
                        Log.d(TAG, "Accelerometer sensor enabled");
                    }
                }, 1500);

                //enable Gyroscope sensor
                enableHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        enableSensor(sConnectedGatt, (UUID) SensorDataModel.allServices.get("Gyroscope").get("GYRO_SERVICE"),
                                (UUID) SensorDataModel.allServices.get("Gyroscope").get("GYRO_CONFIG"), true);

                    }
                }, 2000);

                //enable Magnetometer sensor after 1 sec
                enableHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        enableSensor(sConnectedGatt, (UUID) SensorDataModel.allServices.get("Magnetometer").get("MAGNETOMETER_SERVICE"),
                                (UUID) SensorDataModel.allServices.get("Magnetometer").get("MAGNETOMETER_CONFIG"), true);
                        Log.d(TAG, "Magnetometer sensor enabled");
                    }
                }, 2500);

                //enable notifications for Accelerometer sensor
                enableHandler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        setNotification(sConnectedGatt, (UUID) SensorDataModel.allServices.get("Accelerometer").get("ACCELEROMETER_SERVICE"),
                                (UUID) SensorDataModel.allServices.get("Accelerometer").get("ACCELEROMETER_DATA"), true);
                        Log.d(TAG, "Acc notifications enabled");
                    }
                }, 3000);

                //enable notifications for Gyroscope sensor
                enableHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setNotification(sConnectedGatt, (UUID)SensorDataModel.allServices.get("Gyroscope").get("GYRO_SERVICE"),
                                (UUID)SensorDataModel.allServices.get("Gyroscope").get("GYRO_DATA"), true);
                        Log.d(TAG, "Gyro notifications enabled");
                    }
                }, 3500);

                //enable notifications for Magnetometer sensor
                enableHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setNotification(sConnectedGatt, (UUID)SensorDataModel.allServices.get("Magnetometer").get("MAGNETOMETER_SERVICE"),
                                (UUID)SensorDataModel.allServices.get("Magnetometer").get("MAGNETOMETER_DATA"), true);
                        Log.d(TAG, "Mag notifications enabled");
                    }
                }, 4000);
            }
            else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
        //when a write operation completes
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Write operation successful");

//                //enable notifications for Accelerometer sensor
//                setNotification(sConnectedGatt, (UUID) SensorDataModel.allServices.get("Accelerometer").get("ACCELEROMETER_SERVICE"),
//                                (UUID) SensorDataModel.allServices.get("Accelerometer").get("ACCELEROMETER_DATA"), true);
//                Log.d(TAG, "Acc notifications enabled");
//
//                //enable notifications for Gyroscope sensor
//                enableHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        setNotification(sConnectedGatt, (UUID)SensorDataModel.allServices.get("Gyroscope").get("GYRO_SERVICE"),
//                                (UUID)SensorDataModel.allServices.get("Gyroscope").get("GYRO_DATA"), true);
//                        Log.d(TAG, "Gyro notifications enabled");
//                    }
//                }, 1000);
//
//                //enable notifications for Magnetometer sensor
//                enableHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        setNotification(sConnectedGatt, (UUID)SensorDataModel.allServices.get("Magnetometer").get("MAGNETOMETER_SERVICE"),
//                                (UUID)SensorDataModel.allServices.get("Magnetometer").get("MAGNETOMETER_DATA"), true);
//                        Log.d(TAG, "Mag notifications enabled");
//                    }
//                }, 1000);
            }
            else {
                Log.d(TAG, "Write operation returned status: " + status);
            }
        }
        //when a data read request comes in
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
            if (characteristic.getUuid().equals(SensorDataModel.ACCELEROMETER_DATA)) {
                //Log the value of the sensor and send to SensorDataActivity to display using an Intent
                double[] result = SensorDataModel.extractAccelerometerValues(characteristic);
//                Log.d(TAG, "Accelerometer: x = " + result[0] + " " +
//                                         " y = " + result[1] + " " +
//                                         " z = " + result[2]);
                sendAccMessage(result[0], result[1], result[2]);

            }
            else if (characteristic.getUuid().equals(SensorDataModel.GYRO_DATA)) {
                float[] result = SensorDataModel.extractGyroValues(characteristic);
//                Log.d(TAG, "Gyroscope: x = " + result[0] +
//                                     " y = " + result[1] +
//                                     " z = " + result[2]);
                sendGyroMessage(result[0], result[1], result[2]);
            }

            else if (characteristic.getUuid().equals(SensorDataModel.MAGNETOMETER_DATA)) {
                float[] result = SensorDataModel.extractMagValues(characteristic);
//                Log.d(TAG, "Magnetometer: x = " + result[0] +
//                                        " y = " + result[1] +
//                                        " z = " + result[2]);
                sendMagMessage(result[0], result[1], result[2]);
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

            if(serviceUuid.equals(SensorDataModel.allServices.get("Gyroscope").get("GYRO_SERVICE"))){
                config.setValue(new byte[]{7}); //enable all three sensors for Gyro
                Log.d(TAG, "Gyro sensor enabled");
            }
            else {
                config.setValue(new byte[]{1});
                Log.d(TAG, "Sensor enabled");
            }
        }
        else {
            config.setValue(new byte[]{0});         //Disable sensor
            Log.d(TAG, "Sensor disabled");
        }
        bluetoothGatt.writeCharacteristic(config);  //write to characteristic
    }
    //Changing period by writing to period UUID of each sensor to 5 Hz
    private void changePeriod(BluetoothGatt bluetoothGatt, UUID serviceUuid, UUID periodUuid){
        BluetoothGattService sensorService = bluetoothGatt.getService(serviceUuid);
        BluetoothGattCharacteristic sensorPeriod = sensorService.getCharacteristic(periodUuid);
        sensorPeriod.setValue(new byte[]{20}); //set Period to 20 * 10 ms = 200 ms = 1/5th
        bluetoothGatt.writeCharacteristic(sensorPeriod);
        Log.d(TAG, "Period value after write " +
                sensorPeriod.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0));
    }

    //Setting notification for a service on the device locally and remotely
    //from SensorTag manual
    //must be called after enableSensor() write operation finishes
    private void setNotification(BluetoothGatt bluetoothGatt, UUID serviceUuid, UUID dataUuid, boolean enable){
        final UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        Log.d(TAG, "setNotification() started");

        BluetoothGattService sensorService = bluetoothGatt.getService(serviceUuid);
        BluetoothGattCharacteristic serviceDataCharacteristic = sensorService.getCharacteristic(dataUuid);

        if(enable) { //enabled locally
            bluetoothGatt.setCharacteristicNotification(serviceDataCharacteristic, true);
            BluetoothGattDescriptor configDescriptor = serviceDataCharacteristic.getDescriptor(CCC);
            configDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(configDescriptor); //enabled remotely
            Log.d(TAG, "Notifications set" + configDescriptor.getValue());
        }
        else {       //disabled locally
            bluetoothGatt.setCharacteristicNotification(serviceDataCharacteristic, false);
            BluetoothGattDescriptor configDescriptor = serviceDataCharacteristic.getDescriptor(CCC);
            configDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(configDescriptor); //disabled remotely
            Log.d(TAG, "Notifications disabled");
        }
    }

    //method that broadcasts the result of the magnetometer sensor read using an intent to activities
    private void sendMagMessage(float x, float y, float z){
        //Log.d(TAG, "Broadcasting message magnetometer...");
        Intent intent = new Intent(SensorDataActivity.MAGNETOMETER_INTENT_FILTER);
        //include result with the intent
        intent.putExtra("RESULT x", x);
        intent.putExtra("RESULT y", y);  //String.format("%.2f", y)
        intent.putExtra("RESULT z", z);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //method that broadcasts the result of the accelerometer sensor read using an intent to activities
    private void sendAccMessage(double x, double y, double z){
        //Log.d(TAG, "Broadcasting message accelerometer...");
        Intent intent = new Intent(SensorDataActivity.ACCELEROMETER_INTENT_FILTER);
        //include results with intent, convert double to two decimal places string
        intent.putExtra("RESULT x", x);
        intent.putExtra("RESULT y", y);  //String.format("%.2f", y)
        intent.putExtra("RESULT z", z);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

        //method that broadcasts the result of the magnetometer sensor read using an intent to activities
    private void sendGyroMessage(float x, float y, float z){
         //Log.d(TAG, "Broadcasting message gyroscope...");
         Intent intent = new Intent(SensorDataActivity.GYROSCOPE_INTENT_FILTER);
         //include result with the intent
         intent.putExtra("RESULT x", x);
         intent.putExtra("RESULT y", y);  //String.format("%.2f", y)
         intent.putExtra("RESULT z", z);
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
