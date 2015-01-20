package com.nadia.android.sensortaglog;
//
//import android.content.Context;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.TextView;
//
//import java.util.List;
//
///**
// * Created by nadia on 13/11/14.
// * http://stackoverflow.com/questions/8166497/custom-adapter-for-list-view
// * Adapter to display a list of all recordings made
// */
//public final class CustomRecordingsListAdapter extends ArrayAdapter<RecordingsDataModel> {
//
//    private String TAG = "CustomRecordingsListAdapter";
//    private TextView recID;
//    private TextView tStart;
//    private TextView tEnd;
//
//
//    public CustomRecordingsListAdapter(Context context, int listItemLayoutResourceId) {
//        super(context, listItemLayoutResourceId);
//    }
//
//    //constructor for adapter to use the relative layout file
//    public CustomRecordingsListAdapter(final Context context, int resource, List<RecordingsDataModel> items) {
//        super(context, resource, items);
//    }
//
//    //override getView in order to specify what is displayed where
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        View v = convertView;
//
//        if (v == null) {
//            LayoutInflater vi;
//            vi = LayoutInflater.from(getContext());
//            v = vi.inflate(R.layout.list_item_recordings_layout, null);
//        }
//
//        RecordingsDataModel recording = getItem(position);
//
//        if (recording != null) {
//            Log.d(TAG, "Setting view...");
//            recID  = (TextView)v.findViewById(R.id.recId);
//            tStart = (TextView)v.findViewById(R.id.timestamp_start);
//            tEnd   = (TextView)v.findViewById(R.id.timestamp_end);
//
//
//           if (recID != null) {
//               recID.setText("Recording no: " + String.valueOf(recording.getId()));
//            }
//            else {
//                Log.d(TAG,"RecId returned null" );
//            }
//
//            if (tStart != null) {
//                tStart.setText("from "+recording.getStart());
//            }
//
//            if (tEnd != null) {
//                tEnd.setText("to " +recording.getEnd());
//            }
//        }
//        return v;
//    }
//}

//EXPANDABLE LIST ADAPTER
//http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class RecordingsExpListAdapter extends BaseExpandableListAdapter {

    private Context adapterContext;
    private List<String> listDataHeader; // header titles

    // child data in format of header title, child title
    private HashMap<String, List<String>> listDataChild;

    //constructor
    public RecordingsExpListAdapter(Context context, List<String> listHeaderData,
                                    HashMap<String, List<String>> listChildData) {
        adapterContext = context;
        listDataHeader = listHeaderData;
        listDataChild  = listChildData;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) adapterContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.activity_recordings_list_group, null);
        }

        TextView lblListHeader = (TextView)convertView.findViewById(R.id.expandable_list_header);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public Object getGroup(int groupPosition) {
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
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) adapterContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.activity_recordings_list_item, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.expandable_list_item);

        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return listDataChild.get(listDataHeader.get(groupPosition)).get(childPosititon);
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

