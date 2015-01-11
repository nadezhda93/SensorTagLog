package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
        //unwrap the array of objects and add to adapter to put on screen
        for(int i = 0; i<mRecordings.size(); i++){
            RecordingsDataModel rec = mRecordings.get(i);
            mRecordingsAdapter.add(rec);
            mRecordingsAdapter.notifyDataSetChanged();
            Log.d(TAG, "ID: "     + rec.getId());
            Log.d(TAG, "Tstart: " + rec.getStart());
            Log.d(TAG, "Tend: "   + rec.getEnd());

        }

        //set up listener for the list of recordings
        mRecordingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //get clicked object
                RecordingsDataModel clicked_rec = mRecordingsAdapter.getItem(position);
                Log.d(TAG, "Rec ID: " + clicked_rec.getId() + " was clicked");

                // open new activity using an intent and send in strings of
                //id, start and end timestamps for query in next activity
                final Intent intent = new Intent(RecordingsDataActivity.this, PlotActivity.class);
                intent.putExtra(PlotActivity.EXTRAS_REC_ID,          clicked_rec.getId());
                intent.putExtra(PlotActivity.EXTRAS_START_TIMESTAMP, clicked_rec.getStart());
                intent.putExtra(PlotActivity.EXTRAS_END_TIMESTAMP,   clicked_rec.getEnd());
                //start new PlotActivity
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecordingsAdapter.clear();
        mRecordings.clear();
    }

}
