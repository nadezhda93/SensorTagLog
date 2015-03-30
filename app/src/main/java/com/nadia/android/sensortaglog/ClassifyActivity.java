package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by nadia on 30/03/15.
 * This Activity will receive the recording that was expanded
 * in the PlotActivity and pull out the data from the database
 * and perform LDA classification using 3/4 as training and
 * 1/4 as classification data and show the results to the user
 */
public class ClassifyActivity extends Activity {
    private final String TAG = "ClassifyActivity";
    public static final String EXTRAS_REC_ID = "ExtrasRecId";

    private Intent intent = new Intent();
    private int    mRecId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate started");
        setContentView(R.layout.activity_bluetooth);

        // extract intent information from RecordingsDataActivity
        intent = getIntent();
        mRecId = intent.getIntExtra(EXTRAS_REC_ID, 0);
        Log.d(TAG, "ID selected from RecordingActivity: " + mRecId);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
