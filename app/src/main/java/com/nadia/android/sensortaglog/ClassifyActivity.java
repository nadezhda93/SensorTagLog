package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

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
    private RecordingsDataModel recording;
    private String tableAcc, tableGyro;

    private SensorDataSQLiteHelper db    = new SensorDataSQLiteHelper(this);

    private ArrayList<Float> xAcc = new ArrayList<Float>();
    private ArrayList<Float> yAcc = new ArrayList<Float>();
    private ArrayList<Float> zAcc = new ArrayList<Float>();

    private ArrayList<Float> xGyro = new ArrayList<Float>();
    private ArrayList<Float> yGyro = new ArrayList<Float>();
    private ArrayList<Float> zGyro = new ArrayList<Float>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate started");
        setContentView(R.layout.activity_bluetooth);

        // extract intent information from RecordingsDataActivity
        intent = getIntent();
        mRecId = intent.getIntExtra(EXTRAS_REC_ID, 0);
        Log.d(TAG, "ID selected from RecordingActivity: " + mRecId);

        //get the object that corresponds to mRecId from mRecordings array in RecDataActivity
        for (int i = 0;i<RecordingsDataActivity.mRecordings.size();i++){
            if(RecordingsDataActivity.mRecordings.get(i).getId() == mRecId){
                recording = RecordingsDataActivity.mRecordings.get(i);
            }
        }

        //pull required data out of database
        getClassifyData();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void getClassifyData(){
        //Accelerometer - get x y z values
        tableAcc = "Accelerometer";
        xAcc = db.queryValues(mRecId, tableAcc, "x");
        yAcc = db.queryValues(mRecId, tableAcc, "y");
        zAcc = db.queryValues(mRecId, tableAcc, "z");


        //Gyroscope - get x y z values
        tableGyro = "Gyroscope";
        xGyro = db.queryValues(mRecId, tableGyro, "x");
        yGyro = db.queryValues(mRecId, tableGyro, "y");
        zGyro = db.queryValues(mRecId, tableGyro, "z");
    }
}
