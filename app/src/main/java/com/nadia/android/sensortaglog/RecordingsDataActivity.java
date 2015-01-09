package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by nadia on 09/01/15.
 * Activity to put a list of the data recordings on the screen
 * through a custom adapter class.
 */
public class RecordingsDataActivity extends Activity {
    private static final String TAG = "RecordingsDataActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate started");
        setContentView(R.layout.activity_recordings);

        //populate RecordingsDataModel by making an object for every recording
        //by querying the database and putting the objects on screen


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
