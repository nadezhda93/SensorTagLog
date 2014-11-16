package com.nadia.android.sensortaglog;

import android.bluetooth.BluetoothGattCharacteristic;
/**
 * Created by nadia on 02/11/14.
 * Class used to convert the raw data from SensorTag into required format
 * Formulae obtained directly from manufacturer of SensorTag Texas Instruments
 */
public class SensorTagDataConvert {

    //method for Gyroscope, Magnetometer, Barometer, IR temperature
    //all store 16bit two's complement values in the format LSB MSB
    //this method extracts them
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


    //2. convert Accelerometer sensor values
    public double[] extractAccelerometerValues(final BluetoothGattCharacteristic c){
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


    //3. convert Gyroscope sensor values
    public float[] extractGyroValues(final BluetoothGattCharacteristic c){
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

    //4. convert Humidity sensor values
    public float extractHumidityValues(final BluetoothGattCharacteristic c){
        int a = shortUnSignedAtOffset(c,2);
        //bits [1..0] are status bits and need to be cleared according to the
        //user guide, but iOS code doesn't bother. Minimal impact
        a = a - (a % 4);

        return (-6f) + 125f *(a/65535f);
    }
}
