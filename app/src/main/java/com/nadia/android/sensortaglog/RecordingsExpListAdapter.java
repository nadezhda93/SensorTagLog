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

public class RecordingsExpListAdapter extends BaseExpandableListAdapter {

    private String TAG = "RecordingsExpListAdapter";
    private Context adapterContext;
    private ArrayList<RecordingsDataModel> listDataHeader; // header titles

    private TextView recID;
    private TextView tStart;
    private TextView tEnd;

    private CheckBox childBox;

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
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);
        final RecordingsDataModel recording = getGroup(groupPosition);

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
        }

        //set up listener for the checkbox and change member in RecordingsDataModel for the
        //recording object
        childBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                //get the recording and child for which box was checked
                RecordingsDataModel rec = getGroup(groupPosition);
                String child = (String)getChild(groupPosition, childPosition);

                if(isChecked) {
                    rec.makeSelection(rec, child);
                }
                else{
                    rec.clearSelection(rec,child);
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
}


