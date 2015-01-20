package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;

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

    private ExpandableListView mExpRecordingsListView;
    private RecordingsExpListAdapter mExpRecordingsAdapter;

    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

//   private ArrayList<RecordingsDataModel> mRecordings
//            = new ArrayList<RecordingsDataModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate started");
        setContentView(R.layout.activity_recordings);

        //wire up ExpandableListView widget by resource ID
        mExpRecordingsListView = (ExpandableListView) findViewById(R.id.expandable_list_view);
        //set the adapter to the listView in the layout using default android list layout

        // preparing list data
        prepareListData();

        mExpRecordingsAdapter = new RecordingsExpListAdapter(this,
                listDataHeader, listDataChild);
        mExpRecordingsListView.setAdapter(mExpRecordingsAdapter);

//        //check if the database exists
//        if (!db.doesDatabaseExist()){
//            Toast.makeText(this, "No recordings exist! Please go back.", Toast.LENGTH_LONG).show();
//            Log.d(TAG, "No recordings in database");
//        }
//        //populate RecordingsDataModel by making an object for every recording
//        //by querying the database and putting the objects on screen
//        else {
//            mRecordings = db.queryRecordings();
//            Log.d(TAG, "queryRecordings started");
//        }
//        //unwrap the array of objects and add to adapter to put on screen
//        for(int i = 0; i<mRecordings.size(); i++){
//            RecordingsDataModel rec = mRecordings.get(i);
//            mRecordingsAdapter.add(rec);
//            mRecordingsAdapter.notifyDataSetChanged();
//            Log.d(TAG, "ID: "     + rec.getId());
//            Log.d(TAG, "Tstart: " + rec.getStart());
//            Log.d(TAG, "Tend: "   + rec.getEnd());
//
//        }
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
        //mExpRecordingsAdapter.clear();
        //mExpRecordings.clear();
    }


    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("Top 250");
        listDataHeader.add("Now Showing");
        listDataHeader.add("Coming Soon..");

        // Adding child data
        List<String> top250 = new ArrayList<String>();
        top250.add("The Shawshank Redemption");
        top250.add("The Godfather");
        top250.add("The Godfather: Part II");
        top250.add("Pulp Fiction");
        top250.add("The Good, the Bad and the Ugly");
        top250.add("The Dark Knight");
        top250.add("12 Angry Men");

        List<String> nowShowing = new ArrayList<String>();
        nowShowing.add("The Conjuring");
        nowShowing.add("Despicable Me 2");
        nowShowing.add("Turbo");
        nowShowing.add("Grown Ups 2");
        nowShowing.add("Red 2");
        nowShowing.add("The Wolverine");

        List<String> comingSoon = new ArrayList<String>();
        comingSoon.add("2 Guns");
        comingSoon.add("The Smurfs 2");
        comingSoon.add("The Spectacular Now");
        comingSoon.add("The Canyons");
        comingSoon.add("Europa Report");

        listDataChild.put(listDataHeader.get(0), top250); // Header, Child data
        listDataChild.put(listDataHeader.get(1), nowShowing);
        listDataChild.put(listDataHeader.get(2), comingSoon);
    }

}
