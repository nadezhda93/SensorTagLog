package com.nadia.android.sensortaglog;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by nadia on 22/11/14.
 */
public class SensorDataService extends Service {
    private final static String TAG = "SensorDataService";

    // required to extend Binder in order to make
    // a private bound service (BLE example Android dev)
    public class LocalBinder extends Binder {
        SensorDataService getService() {
            return SensorDataService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // we are going to make sure all connections to Gatt device
        // are closed
        // FIX:ME
        return super.onUnbind(intent);
    }

    public static boolean initialise() {
        Log.d(TAG, "initialise() called");
        return true;
    }
}
