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

import com.androidplot.Plot;

import java.security.acl.Group;
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
    public static ArrayList<RecordingsDataModel> mRecordings
            = new ArrayList<RecordingsDataModel>();

    private ArrayList<String> children = new ArrayList<String>();

    private CheckBox checkBox;
    private int lastExpandedPosition = -1;
    public static Boolean selectionAxisMade   = false;
    public static Boolean selectionSensorMade = false;

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

        checkBox = (CheckBox)findViewById(R.id.expandable_list_item);

        //listener for group click to override default actions, stop auto-scrolling
        mExpRecordingsListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                //prevent default scrolling when groups open and close
                if(parent.isGroupExpanded(groupPosition)){
                    parent.collapseGroup(groupPosition);
                }
                else{
                    boolean animateExpansion = false;
                    parent.expandGroup(groupPosition,animateExpansion);
                }
                //telling the listView we have handled the group click, and don't want the default actions.
                return true;
            }
        });


        //listener for group expand, close previous group when new is open
        mExpRecordingsListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                RecordingsDataModel rec = mExpRecordingsAdapter.getGroup(groupPosition);
                Log.d(TAG, "Group expanded: " + rec.getId());
                if(lastExpandedPosition != -1 && groupPosition != lastExpandedPosition) {
                    mExpRecordingsListView.collapseGroup(lastExpandedPosition);
                    //clear any selections for lastExpandedPosition
                    RecordingsDataModel lastRec =
                            mExpRecordingsAdapter.getGroup(lastExpandedPosition);
                    for (int i = 0; i < children.size(); i++){
                        lastRec.clearSelection(lastRec,children.get(i));
                    }
                    selectionAxisMade = false;
                    selectionSensorMade = false;
                }
                lastExpandedPosition = groupPosition;
            }
        });
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
               //start new activity when both selections have been made
               if (selectionSensorMade && selectionAxisMade){
                    //start Plot Activity
                    RecordingsDataModel rec = mExpRecordingsAdapter.getGroup(lastExpandedPosition);
                    Log.d(TAG, "Start PlotActivity!!");
                    final Intent intent = new Intent(RecordingsDataActivity.this,
                                                    PlotActivity.class);
                    intent.putExtra(PlotActivity.EXTRAS_REC_ID, rec.getId());
                    startActivity(intent);
                }
               else{
                    Toast.makeText(RecordingsDataActivity.this, "You must select attributes to plot.",
                                        Toast.LENGTH_SHORT).show();
               }
               return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //set up child data
    private void prepareListData() {
        listDataChild = new HashMap<RecordingsDataModel, ArrayList<String>>();
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
