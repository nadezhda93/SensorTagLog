package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by nadia on 09/01/15.
 * Activity to put a list of the data recordings on the screen
 * through a custom adapter class.
 */
public class RecordingsDataActivity extends Activity {

    private static final String TAG = "RecordingsDataActivity";
    private SensorDataSQLiteHelper db = new SensorDataSQLiteHelper(this);

    private ListView mRecordingsListView;
    private CustomRecordingsListAdapter mRecordingsAdapter;
    private ArrayList<RecordingsDataModel> mRecordings
            = new ArrayList<RecordingsDataModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate started");
        setContentView(R.layout.activity_recordings);

        //wire up ListView widget by resource ID
        mRecordingsListView = (ListView) findViewById(R.id.recordings_found_list);
        //set the adapter to the listView in the layout using default android list layout
        mRecordingsAdapter = new CustomRecordingsListAdapter(this,
                android.R.layout.simple_list_item_1, mRecordings);
        mRecordingsListView.setAdapter(mRecordingsAdapter);

        //check if the database exists
        if (!db.doesDatabaseExist()){
            Toast.makeText(this, "No recordings exist! Please go back.", Toast.LENGTH_LONG).show();
            Log.d(TAG, "No recordings in database");
        }
        //populate RecordingsDataModel by making an object for every recording
        //by querying the database and putting the objects on screen
        else {
            mRecordings = db.queryRecordings();
            Log.d(TAG, "queryRecordings started");

        }


        for(int i = 0; i<mRecordings.size(); i++){
            RecordingsDataModel rec = mRecordings.get(i);
            mRecordingsAdapter.add(rec);
            mRecordingsAdapter.notifyDataSetChanged();
            Log.d(TAG, "ID: " + rec.getId());
            Log.d(TAG, "Tstart: " +rec.getStart());
            Log.d(TAG, "Tend: "   +rec.getEnd());

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecordingsAdapter.clear();
        mRecordings.clear();
    }

}
