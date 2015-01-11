package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by nadia on 11/01/15.
 * Activity which will query the database for the values of the
 * sensors and plot them on a graph from AndroidPlot API library
 */
public class PlotActivity extends Activity {

    private final String TAG = "PlotActivity";

    public static final String EXTRAS_REC_ID             = "h";
    public static final String EXTRAS_START_TIMESTAMP = "ExtrasStartTimestamp";
    public static final String EXTRAS_END_TIMESTAMP   = "ExtrasEndTimestamp";

    private Intent intent = new Intent();

    private int    mRecId;
    private String mRecStartTimestamp;
    private String mRecEndTimestamp;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordings);

        // extract intent information from RecordingsDataActivity
        intent             = getIntent();
        mRecId             = intent.getIntExtra(EXTRAS_REC_ID, 0);
        mRecStartTimestamp = intent.getStringExtra(EXTRAS_START_TIMESTAMP);
        mRecEndTimestamp   = intent.getStringExtra(EXTRAS_END_TIMESTAMP);
        Log.d(TAG, "ID: " + mRecId + " Start: " + mRecStartTimestamp + " End: " + mRecEndTimestamp);
    }
}
