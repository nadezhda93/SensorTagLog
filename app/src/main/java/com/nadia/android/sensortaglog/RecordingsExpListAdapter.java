package com.nadia.android.sensortaglog;


///**
// * Created by nadia on 13/11/14.
// * http://stackoverflow.com/questions/8166497/custom-adapter-for-list-view
// * Adapter to display a list of all recordings made
// */

//EXPANDABLE LIST ADAPTER
//http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/

import java.util.ArrayList;
import java.util.HashMap;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class RecordingsExpListAdapter extends BaseExpandableListAdapter {

    private String TAG = "RecordingsExpListAdapter";
    private Context adapterContext;
    private ArrayList<RecordingsDataModel> listDataHeader; // header titles

    private TextView recID;
    private TextView tStart;
    private TextView tEnd;

    private CheckBox childBox;

    private ArrayList<String> sensors = new ArrayList<String>();
    private ArrayList<String> axes = new ArrayList<String>();


    // child data in format of header title, child title
    private HashMap<RecordingsDataModel, ArrayList<String>> listDataChild;

    //constructor
    public RecordingsExpListAdapter(Context context, ArrayList<RecordingsDataModel> listHeaderData,
                                    HashMap<RecordingsDataModel, ArrayList<String>> listChildData) {
        adapterContext = context;
        listDataHeader = listHeaderData;
        listDataChild  = listChildData;
    }

    @Override
    public View getGroupView(int position, boolean isExpanded, View convertView, ViewGroup parent) {

        RecordingsDataModel recording = getGroup(position);
        //Log.d(TAG, "Got recording " + recording.getId());

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(adapterContext);
            convertView = inflater.inflate(R.layout.activity_recordings_list_group, null);
        }

        if(recording != null) {
            recID  = (TextView)convertView.findViewById(R.id.expandable_header_recId);
            recID.setText("Recording " + String.valueOf(recording.getId()));

            tStart = (TextView)convertView.findViewById(R.id.expandable_subheader_timestamp_start);
            tStart.setText(recording.getStart());

            tEnd   = (TextView)convertView.findViewById(R.id.expandable_subheader_timestamp_end);
            tEnd.setText(recording.getEnd());
        }
        return convertView;
    }

    @Override
    public RecordingsDataModel getGroup(int groupPosition) {
        return listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }


    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, final ViewGroup parent) {
        Log.d(TAG, "got child view");
        final String childText = (String) getChild(groupPosition, childPosition);
        final RecordingsDataModel recording = getGroup(groupPosition);
        setSensors();

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(adapterContext);
            convertView = inflater.inflate(R.layout.activity_recordings_list_item_v2, null);
        }

        childBox = (CheckBox) convertView.findViewById(R.id.expandable_list_item);
        childBox.setText(childText);

        //check whether all attributes are disabled as set all checkboxes unchecked
        if(!recording.getAcc() && !recording.getGyro() && !recording.getMag() &&
                !recording.getX() && !recording.getY() && !recording.getZ()){
            childBox.setChecked(false);
            RecordingsDataActivity.selectionAxisMade = false;
            RecordingsDataActivity.selectionSensorMade = false;
        }

        //set up listener for the checkbox and change member in RecordingsDataModel for the
        //recording object
        childBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String child = (String) getChild(groupPosition, childPosition);
                Log.d(TAG, "Child check change here: " + child);

                if (isChecked) {
                    //did we select a sensor again?
                    if (sensors.contains(child) && RecordingsDataActivity.selectionSensorMade) {
                        Log.d(TAG, "selected sensor again");
                        Toast.makeText(parent.getContext(), "Select only one sensor. " +
                                "First selection only will be counted.", Toast.LENGTH_LONG).show();
                    }
                    //did we select axis?
                    else if (axes.contains(child)) {
                        Log.d(TAG, "selected axis");
                        recording.makeSelection(recording, child);
                        RecordingsDataActivity.selectionAxisMade = true;
                    }
                    //we selected sensor for the first time
                    else {
                        Log.d(TAG, "selected sensor first time");
                        recording.makeSelection(recording, child);
                        RecordingsDataActivity.selectionSensorMade = true;
                    }
                }
                else {
                    recording.clearSelection(recording, child);
                    //check if anything is chosen, reset boolean for Plot button
                    if (!recording.getAcc() && !recording.getGyro() && !recording.getMag()) {
                        RecordingsDataActivity.selectionSensorMade = false;
                    } else if (!recording.getX() && !recording.getY() && !recording.getZ()) {
                        RecordingsDataActivity.selectionAxisMade   = false;
                    }
                }
            }
        }
        );
        return convertView;
        }

        @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listDataChild.get(listDataHeader.get(groupPosition)).size();
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void setSensors(){
        sensors.add("Accelerometer");
        sensors.add("Gyroscope");
        sensors.add("Magnetometer");

        axes.add("X axis");
        axes.add("Y axis");
        axes.add("Z axis");
    }
}



