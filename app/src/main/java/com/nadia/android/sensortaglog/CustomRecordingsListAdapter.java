package com.nadia.android.sensortaglog;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by nadia on 13/11/14.
 * http://stackoverflow.com/questions/8166497/custom-adapter-for-list-view
 * Adapter to display a list of all recordings made
 */
public final class CustomRecordingsListAdapter extends ArrayAdapter<RecordingsDataModel> {

    private String TAG = "CustomRecordingsListAdapter";
    private TextView recID;
    private TextView tStart;
    private TextView tEnd;


    public CustomRecordingsListAdapter(Context context, int listItemLayoutResourceId) {
        super(context, listItemLayoutResourceId);
    }

    //constructor for adapter to use the relative layout file
    public CustomRecordingsListAdapter(final Context context, int resource, List<RecordingsDataModel> items) {
        super(context, resource, items);
    }

    //override getView in order to specify what is displayed where
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_item_recordings_layout, null);
        }

        RecordingsDataModel recording = getItem(position);

        if (recording != null) {
            Log.d(TAG, "Setting view...");
            recID  = (TextView)v.findViewById(R.id.recId);
            tStart = (TextView)v.findViewById(R.id.timestamp_start);
            tEnd   = (TextView)v.findViewById(R.id.timestamp_end);


           if (recID != null) {
               recID.setText("Recording no: " + String.valueOf(recording.getId()));
            }
            else {
                Log.d(TAG,"RecId returned null" );
            }

            if (tStart != null) {
                tStart.setText("from "+recording.getStart());
            }

            if (tEnd != null) {
                tEnd.setText("to " +recording.getEnd());
            }
        }
        return v;
    }
}
