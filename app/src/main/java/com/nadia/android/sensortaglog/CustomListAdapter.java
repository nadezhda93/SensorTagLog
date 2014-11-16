package com.nadia.android.sensortaglog;

import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.List;

/**
 * Created by nadia on 13/11/14.
 * http://stackoverflow.com/questions/8166497/custom-adapter-for-list-view
 */
public final class CustomListAdapter extends ArrayAdapter<BluetoothDevice> {

    private String TAG = "CustomListAdapter";

    public CustomListAdapter(Context context, int listItemLayoutResourceId) {
        super(context, listItemLayoutResourceId);
    }

    //constructor for adapter to use the relative layout file
    public CustomListAdapter(final Context context, int resource, List<BluetoothDevice> items) {
        super(context, resource, items);
    }

    //override getView in order to specify what is displayed where
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_item_layout, null);
        }

        BluetoothDevice device = getItem(position);

        if (device != null) {
            TextView deviceName = (TextView) v.findViewById(R.id.ble_device_name_item);
            TextView deviceAddress = (TextView) v.findViewById(R.id.ble_device_address_item);


            if (deviceName != null) {
                deviceName.setText(device.getName());
                Log.d(TAG, "Device returned a valid name");
            }
            else {
                Log.d(TAG,"Device.getName() returned null" );
            }

            if (deviceAddress != null) {
                deviceAddress.setText(device.getAddress());
                Log.d(TAG, "Device returned valid address");
            }
        }
        return v;
    }
}
