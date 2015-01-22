package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.androidplot.Plot;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Created by nadia on 11/01/15.
 * Activity which will query the database for the values of the
 * sensors and plot them on a graph from AndroidPlot API library
 * http://androidplot.com/docs/
 */
public class PlotActivity extends Activity {

    private final String TAG = "PlotActivity";

    public static final String EXTRAS_REC_ID          = "ExtrasRecId";

    private Intent intent = new Intent();
    private int    mRecId;
    private XYPlot plot;

    private SensorDataSQLiteHelper db    = new SensorDataSQLiteHelper(this);
    private ArrayList<String> timestamps = new ArrayList<String>();
    private SimpleDateFormat sdf         = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");

    private ArrayList<Long> parsedTimestamps = new ArrayList<Long>();
    private ArrayList<Float> xValuesAcc;

    private RecordingsDataModel recording;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);

        // extract intent information from RecordingsDataActivity
        intent = getIntent();
        mRecId = intent.getIntExtra(EXTRAS_REC_ID, 0);
        Log.d(TAG, "ID selected: " + mRecId);

        //get the object that corresponds to mRecId from mRecordings array in RecDataActivity
        for (int i = 0;i<RecordingsDataActivity.mRecordings.size();i++){
            if(RecordingsDataActivity.mRecordings.get(i).getId() == mRecId){
                recording = RecordingsDataActivity.mRecordings.get(i);
            }
        }
        Log.d(TAG, "Recording ID: "+ recording.getId());

        //x values (Accelerometer timestamps)
        timestamps       = db.queryTimestamps(mRecId);
        parsedTimestamps = parseTimestamps(timestamps);
        xValuesAcc = db.queryValues(mRecId, "Accelerometer", "x");

        //initialise XYPlot reference:
        plot = (XYPlot)findViewById(R.id.mySimpleXYPlot);

        // Turn the arrays into XYSeries:
        // SimpleXYSeries takes a List so turn array into a List
        // Y_VALS_ONLY means use the element index as the x value
        // Set the display title of the series as Series1
        XYSeries series1 = new SimpleXYSeries(parsedTimestamps,
                                              xValuesAcc,
                                              "X Accelerometer");
        plot.setDomainLabel("Time");
        plot.setRangeLabel("Value");
        plot.setDomainStep(XYStepMode.SUBDIVIDE, parsedTimestamps.size());

        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getApplicationContext(), R.xml.line_point_formatter_with_plf1);
        //remove point labels
        series1Format.setPointLabelFormatter(null);
        //reformat timestamps
        plot.setDomainValueFormat(new Format() {
            // create a simple date format that changes the way the date looks
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(":ss");

                // because our timestamps are in seconds and SimpleDateFormat expects milliseconds
                // we multiply our timestamp by 1000:
                long timestamp = ((Number) obj).longValue();
                Date date = new Date(timestamp);
                return dateFormat.format(date, toAppendTo, pos);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;

            }
        });
        //add a new series to the xyplot:
        plot.addSeries(series1, series1Format);

//        LineAndPointFormatter series2Format = new LineAndPointFormatter();
//        series2Format.setPointLabelFormatter(new PointLabelFormatter());
//        series2Format.configure(getApplicationContext(), R.xml.line_point_formatter_with_plf2);
//        plot.addSeries(series2, series2Format);

        // reduce the number of range labels
        plot.setTicksPerRangeLabel(1);
        plot.setTicksPerDomainLabel(2);
 }
    //parse timestamp from a String into milliseconds since epoch time for plot
    private ArrayList<Long> parseTimestamps(ArrayList<String> stringTimestamps){
        ArrayList<Long> parsedTimestamps = new ArrayList<Long>();

        for(int i = 0; i < stringTimestamps.size(); i++){
            Log.d(TAG, "SIZE = " + stringTimestamps.size());
            ParsePosition p = new ParsePosition(0);
            Date date = sdf.parse(stringTimestamps.get(i), p);
            Log.d(TAG, "NEWTimestamp "+ i +" = " + stringTimestamps.get(i) + " = "+ date.getTime());
            parsedTimestamps.add(date.getTime());
        }
        return parsedTimestamps;
    }
}
