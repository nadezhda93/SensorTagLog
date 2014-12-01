package com.nadia.android.sensortaglog;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by nadia on 02/11/14.
 * Class used to convert the raw data from SensorTag into required format
 * Stores UUIDs of services, data and config characteristics for each sensor
 * Formulae obtained directly from manufacturer of SensorTag Texas Instruments
 */
public class SensorDataModel {


    //1. IR temp UUIDs
    private static final UUID IR_SERVICE = UUID.fromString("f000aa00-0451-4000-b000-000000000000");
    public static final UUID IR_DATA = UUID.fromString("f000aa01-0451-4000-b000-000000000000");
    private static final UUID IR_CONFIG = UUID.fromString("f000aa02-0451-4000-b000-000000000000");

    //2. Accelerometer UUIDs
    private static final UUID ACCELEROMETER_SERVICE = UUID.fromString("f000aa10-0451-4000-b000-000000000000");
    public static final UUID ACCELEROMETER_DATA = UUID.fromString("f000aa11-0451-4000-b000-000000000000");
    private static final UUID ACCELEROMETER_CONFIG = UUID.fromString("f000aa12-0451-4000-b000-000000000000");

    //3. Gyroscope UUIDs
    private static final UUID GYRO_SERVICE = UUID.fromString("f000aa50-0451-4000-b000-000000000000");
    public static final UUID GYRO_DATA = UUID.fromString("f000aa51-0451-4000-b000-000000000000");
    private static final UUID GYRO_CONFIG = UUID.fromString("f000aa52-0451-4000-b000-000000000000");

    //4. Humidity Service UUID
    private static final UUID HUMIDITY_SERVICE = UUID.fromString("f000aa20-0451-4000-b000-000000000000");
    public static final UUID HUMIDITY_DATA = UUID.fromString("f000aa21-0451-4000-b000-000000000000");
    private static final UUID HUMIDITY_CONFIG = UUID.fromString("f000aa22-0451-4000-b000-000000000000");

    //5. Magnetometer UUIDs
    private static final UUID MAGNETOMETER_SERVICE = UUID.fromString("f000aa30-0451-4000-b000-000000000000");
    public static final UUID MAGNETOMETER_DATA = UUID.fromString("f000aa31-0451-4000-b000-000000000000");
    private static final UUID MAGNETOMETER_CONFIG = UUID.fromString("f000aa32-0451-4000-b000-000000000000");

    //6. Barometer UUIDs
    private static final UUID BAROMETER_SERVICE = UUID.fromString("f000aa40-0451-4000-b000-000000000000");
    public static final UUID BAROMETER_DATA = UUID.fromString("f000aa41-0451-4000-b000-000000000000");
    private static final UUID BAROMETER_CONFIG = UUID.fromString("f000aa42-0451-4000-b000-000000000000");

    //Hashmap containing Name of service and another HASHMAP of the UUIDs
    public static HashMap<String, HashMap> allServices  = new HashMap<String, HashMap>();
    //Hashmap containing NAME of characteristic and UUID for each
    private static HashMap<String, UUID> humidity       = new HashMap<String, UUID>();
    private static HashMap<String, UUID> accelerometer  = new HashMap<String, UUID>();
    private static HashMap<String, UUID> gyroscope      = new HashMap<String, UUID>();
    private static HashMap<String, UUID> magnetometer   = new HashMap<String, UUID>();

    public static void populateMap(){
        humidity.put("HUMIDITY_SERVICE", HUMIDITY_SERVICE);
        humidity.put("HUMIDITY_DATA",    HUMIDITY_DATA);
        humidity.put("HUMIDITY_CONFIG",  HUMIDITY_CONFIG);

        allServices.put("Humidity", humidity);

        accelerometer.put("ACCELEROMETER_SERVICE", ACCELEROMETER_SERVICE);
        accelerometer.put("ACCELEROMETER_DATA",    ACCELEROMETER_DATA);
        accelerometer.put("ACCELEROMETER_CONFIG",  ACCELEROMETER_CONFIG);

        allServices.put("Accelerometer", accelerometer);

        gyroscope.put("GYRO_SERVICE", GYRO_SERVICE);
        gyroscope.put("GYRO_DATA",    GYRO_DATA);
        gyroscope.put("GYRO_CONFIG",  GYRO_CONFIG);

        allServices.put("Gyroscope", gyroscope);

        magnetometer.put("MAGNETOMETER_SERVICE", MAGNETOMETER_SERVICE);
        magnetometer.put("MAGNETOMETER_DATA",    MAGNETOMETER_DATA);
        magnetometer.put("MAGNETOMETER_CONFIG",  MAGNETOMETER_CONFIG);

        allServices.put("Magnetometer", magnetometer);

    }

    //method for Gyroscope, Magnetometer, Barometer, IR temperature
    //all store 16bit two's complement values in the format LSB MSB
    //this method extracts them from the characteristic
    private static int shortSignedAtOffset(BluetoothGattCharacteristic c, int offset){
        int lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        int upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, offset + 1); //interpret MSB as signed

        return (upperByte << 8) + lowerByte;
    }

    private static int shortUnSignedAtOffset(BluetoothGattCharacteristic c, int offset){
        int lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        int upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1); //interpret MSB as signed

        return (upperByte << 8) + lowerByte;
    }





    //1. convert Accelerometer sensor values
    public static double[] extractAccelerometerValues(final BluetoothGattCharacteristic c){
        //Accelerometer has range [-2g, 2g] with unit (1/64)g
        //so divide by 64 to get g
        //The z value is multiplied by -1 to coincide with how pos y direction
        //is arbitrarily defined
        double[] accData = new double[3];
        int x = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
        int y = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 1);
        int z = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 2)* -1;

        accData[0] = x/64.0;
        accData[1] = y/64.0;
        accData[2] = z/64.0;


        //return an array of three values corresponding to x,y,z co-ords
        return accData;
    }


    //2. convert Gyroscope sensor values
    public static float[] extractGyroValues(final BluetoothGattCharacteristic c){
        //x,y,z not in order
        float y = shortSignedAtOffset(c,0) * (500f / 65536f) * -1;
        float x = shortSignedAtOffset(c,2) * (500f / 65536f);
        float z = shortSignedAtOffset(c,4) * (500f / 65536f);

        float[] gyroData = new float[3];

        gyroData[0] = x;
        gyroData[1] = y;
        gyroData[2] = z;

        return gyroData;
    }

    //3.Convert Magnetometer values
    public static float[] extractMagValues(final BluetoothGattCharacteristic c){
        //Multiply x and y with -1 so values correspond to TI pictures
        float x = shortSignedAtOffset(c,0) * (2000f / 65536f) * -1;
        float y = shortSignedAtOffset(c,2) * (2000f / 65536f) * -1;
        float z = shortSignedAtOffset(c,4) * (2000f / 65536f);

        float[] magData = new float[3];

        magData[0] = x;
        magData[1] = y;
        magData[2] = z;

        return magData;
    }


    //4. convert Humidity sensor values
    public static float extractHumidityValues(final BluetoothGattCharacteristic c){
        int a = shortUnSignedAtOffset(c,2);
        //bits [1..0] are status bits and need to be cleared according to the
        //user guide, but iOS code doesn't bother. Minimal impact
        a = a - (a % 4);

        return (-6f) + 125f *(a/65535f);
    }
}
