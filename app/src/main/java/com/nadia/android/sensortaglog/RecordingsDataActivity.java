package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nadia on 09/01/15.
 * Activity to put a list of the data recordings on the screen
 * through a custom adapter class.
 */
public class RecordingsDataActivity extends Activity {

    private static final String TAG = "RecordingsDataActivity";
    private SensorDataSQLiteHelper db = new SensorDataSQLiteHelper(this);

    private ExpandableListView       mExpRecordingsListView;
    private RecordingsExpListAdapter mExpRecordingsAdapter;

    private HashMap<RecordingsDataModel, ArrayList<String>> listDataChild;
    private ArrayList<RecordingsDataModel> mRecordings
            = new ArrayList<RecordingsDataModel>();

    private CheckBox mAcc;
    private CheckBox mGyro;
    private CheckBox mMag;

    private CheckBox mX;
    private CheckBox mY;
    private CheckBox mZ;

    private Button mPlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate started");
        setContentView(R.layout.activity_recordings);

        //wire up ExpandableListView widget by resource ID
        mExpRecordingsListView = (ExpandableListView) findViewById(R.id.expandable_list_view);
        mExpRecordingsListView.setIndicatorBounds(60, 20);

        // preparing child data
        prepareListData();

        //set the adapter to the listView in the layout using default android list layout
        mExpRecordingsAdapter = new RecordingsExpListAdapter(this, mRecordings, listDataChild);
        mExpRecordingsListView.setAdapter(mExpRecordingsAdapter);


        //set up listeners for the checkboxes and buttons in the child
        mExpRecordingsListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view,
                                        int groupPosition , int childPosition, long id) {
            RecordingsDataModel recording = mExpRecordingsAdapter.getGroup(groupPosition);
            mAcc = (CheckBox)mExpRecordingsAdapter.getChild(groupPosition,childPosition);

            Log.d(TAG, "Clicked " + recording.getId());
            Log.d(TAG, "pressed " + mAcc);
                return false;
            }
        });


//
//        //set up listener for the list of recordings
//        mRecordingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                //get clicked object
//                RecordingsDataModel clicked_rec = mRecordingsAdapter.getItem(position);
//                Log.d(TAG, "Rec ID: " + clicked_rec.getId() + " was clicked");
//
//                // open new activity using an intent and send in strings of
//                //id, start and end timestamps for query in next activity
//                final Intent intent = new Intent(RecordingsDataActivity.this, PlotActivity.class);
//                intent.putExtra(PlotActivity.EXTRAS_REC_ID,          clicked_rec.getId());
//                intent.putExtra(PlotActivity.EXTRAS_START_TIMESTAMP, clicked_rec.getStart());
//                intent.putExtra(PlotActivity.EXTRAS_END_TIMESTAMP,   clicked_rec.getEnd());
//                //start new PlotActivity
//                startActivity(intent);
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecordings.clear();
    }


    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataChild = new HashMap<RecordingsDataModel, ArrayList<String>>();
        ArrayList<String> children = new ArrayList<String>();

        children.add("A child");

        //check if the database exists
        if (!db.doesDatabaseExist()){
            Toast.makeText(this, "No recordings exist! Please go back.", Toast.LENGTH_LONG).show();
            Log.d(TAG, "No recordings in database");
        }
        //populate RecordingsDataModel by making an object for every recording
        //by querying the database and putting the objects on screen
        else {
            mRecordings = db.queryRecordings();
            Log.d(TAG, "Recordings empty? " + mRecordings.isEmpty());


            for (int i = 0; i < mRecordings.size(); i++) {
                listDataChild.put(mRecordings.get(i), children);
                Log.d(TAG, "Success!!");
            }
            Log.d(TAG, "listDataChild empty? " + listDataChild.isEmpty());
        }
    }

}
