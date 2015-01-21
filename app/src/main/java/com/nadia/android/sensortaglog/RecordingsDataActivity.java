package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

    private CheckBox checkBox;

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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecordings.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recordings, menu);
        return true;
    }

    //callback method to respond to clicks of buttons in the action bar or overflow menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_plot:
                //do something when plot is pressed
                //start Plot Activity

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //set up child data
    private void prepareListData() {
        listDataChild = new HashMap<RecordingsDataModel, ArrayList<String>>();
        ArrayList<String> children = new ArrayList<String>();
        //strings for children check boxes
        children.add("Accelerometer");
        children.add("Gyroscope");
        children.add("Magnetometer");
        children.add("X axis");
        children.add("Y axis");
        children.add("Z axis");

        //check if the database exists
        if (!db.doesDatabaseExist()){
            Toast.makeText(this, "No recordings exist! Please go back.", Toast.LENGTH_LONG).show();
            Log.d(TAG, "No recordings in database");
        }
        //populate RecordingsDataModel by making an object for every recording
        //by querying the database and putting the objects on screen
        //assign children check boxes
        else {
            mRecordings = db.queryRecordings();
            Log.d(TAG, "Recordings empty? " + mRecordings.isEmpty());

            for (int i = 0; i < mRecordings.size(); i++) {
                //assign children to each recording object
                listDataChild.put(mRecordings.get(i), children);
            }
            Log.d(TAG, "listDataChild empty? " + listDataChild.isEmpty());
        }
    }

}
