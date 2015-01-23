package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;

import com.androidplot.Plot;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;


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

    private ArrayList<Float> xValuesAcc = new ArrayList<Float>();
    private ArrayList<Float> yValuesAcc = new ArrayList<Float>();
    private ArrayList<Float> zValuesAcc = new ArrayList<Float>();

    private ArrayList<Float> xValuesGyro = new ArrayList<Float>();
    private ArrayList<Float> yValuesGyro = new ArrayList<Float>();
    private ArrayList<Float> zValuesGyro = new ArrayList<Float>();

    private ArrayList<Float> xValuesMag = new ArrayList<Float>();
    private ArrayList<Float> yValuesMag = new ArrayList<Float>();
    private ArrayList<Float> zValuesMag = new ArrayList<Float>();

    private int enabledAxes = 0;
    private ArrayList<XYSeries> xySeries = new ArrayList<XYSeries>();
    private ArrayList<LineAndPointFormatter> seriesFormat = new ArrayList<LineAndPointFormatter>();

    //random colours for series
    private Random rnd = new Random();

    //array of the xy series for each axis value
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


        //x values (Accelerometer timestamps) for all sensors
        timestamps       = db.queryTimestamps(mRecId);
        parsedTimestamps = parseTimestamps(timestamps);

        //y values - dependent on selection
        getData();

        //initialise XYPlot reference:
        plot = (XYPlot)findViewById(R.id.mySimpleXYPlot);
        plot.setDomainLabel("Time (s)");
        plot.setRangeLabel("Value");
        plot.setDomainStep(XYStepMode.SUBDIVIDE, parsedTimestamps.size());
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
        // reduce the number of range labels
        plot.setTicksPerRangeLabel(1);
        plot.setTicksPerDomainLabel(2);


        //repeat the same to set up the plot
        Log.d(TAG, "Axes end: " + enabledAxes);
        for (int j = 0; j < enabledAxes; j++){
            setXYSeries();
            setFormatters();

            //remove point labels for each format
            seriesFormat.get(j).setPointLabelFormatter(null);

            //add series to plot
            plot.addSeries(xySeries.get(j), seriesFormat.get(j));
            Log.d(TAG, "Series added to plot");
        }

 }
    //parse timestamp from a String into milliseconds since epoch time for plot
    private ArrayList<Long> parseTimestamps(ArrayList<String> stringTimestamps){
        ArrayList<Long> parsedTimestamps = new ArrayList<Long>();

        for(int i = 0; i < stringTimestamps.size(); i++){
            Log.d(TAG, "SIZE = " + stringTimestamps.size());
            ParsePosition p = new ParsePosition(0);
            Date date = sdf.parse(stringTimestamps.get(i), p);
            //Log.d(TAG, "NEWTimestamp "+ i +" = " + stringTimestamps.get(i) + " = "+ date.getTime());
            parsedTimestamps.add(date.getTime());
        }
        return parsedTimestamps;
    }
    private void getData(){
        //Accelerometer
        if(recording.getAcc()){
            //get values for x y z as needed
            if(recording.getX()){
                Log.d(TAG, "Got X");
                xValuesAcc = db.queryValues(mRecId, "Accelerometer", "x");
                enabledAxes += 1;
                Log.d(TAG, "AxesX: " + enabledAxes);
            }
            if(recording.getY()){
                Log.d(TAG, "Got Y");
                yValuesAcc = db.queryValues(mRecId, "Accelerometer", "y");
                enabledAxes += 1;
                Log.d(TAG, "AxesY: " + enabledAxes);
            }
            if(recording.getZ()){
                Log.d(TAG, "Got Z");
                zValuesAcc = db.queryValues(mRecId, "Accelerometer", "z");
                enabledAxes += 1;
                Log.d(TAG, "AxesZ: " + enabledAxes);
            }
        }
        //Gyroscope
        if(recording.getGyro()){
            if(recording.getX()){
                xValuesGyro = db.queryValues(mRecId, "Gyroscope", "x");
                Log.d(TAG, "Size Gyro x = " + xValuesGyro.size());
                enabledAxes += 1;
                Log.d(TAG, "AxesX: " + enabledAxes);
            }
            if(recording.getY()){
                yValuesGyro = db.queryValues(mRecId, "Gyroscope", "y");
                enabledAxes += 1;
                Log.d(TAG, "AxesY: " + enabledAxes);
            }
            if(recording.getZ()){
                zValuesGyro = db.queryValues(mRecId, "Gyroscope", "z");
                enabledAxes += 1;
                Log.d(TAG, "AxesZ: " + enabledAxes);
            }
        }
        //Magnetometer
        if(recording.getMag()){
            if(recording.getX()){
                xValuesMag = db.queryValues(mRecId, "Magnetometer", "x");
                enabledAxes += 1;
                Log.d(TAG, "AxesX: " + enabledAxes);
            }
            if(recording.getY()){
                yValuesMag = db.queryValues(mRecId, "Magnetometer", "y");
                enabledAxes += 1;
                Log.d(TAG, "AxesY: " + enabledAxes);
            }
            if(recording.getZ()){
                zValuesMag = db.queryValues(mRecId, "Magnetometer", "z");
                enabledAxes += 1;
                Log.d(TAG, "AxesZ: " + enabledAxes);
            }

        }
    }
    private void setXYSeries(){
        // Turn the arrays into XYSeries:
        // SimpleXYSeries takes a List so turn array into a List
        // Y_VALS_ONLY means use the element index as the x value
        // Set the display title of the series
        if(recording.getAcc()) {
            if(recording.getX()) {
                xySeries.add(new SimpleXYSeries(parsedTimestamps, xValuesAcc, "X Acc"));
                Log.d(TAG, "X added to series");
            }
            if(recording.getY()) {
                xySeries.add(new SimpleXYSeries(parsedTimestamps, yValuesAcc, "Y Acc"));
                Log.d(TAG, "Y added to series");
            }
            if(recording.getZ()) {
                xySeries.add(new SimpleXYSeries(parsedTimestamps, zValuesAcc, "Z Acc"));
                Log.d(TAG, "Y added to series");
            }
        }
        if(recording.getGyro()){
            if(recording.getX()) {
                xySeries.add(new SimpleXYSeries(parsedTimestamps, xValuesGyro, "X Gyro"));
                Log.d(TAG, "X added to series");
            }
            if(recording.getY()) {
                xySeries.add(new SimpleXYSeries(parsedTimestamps, yValuesGyro, "Y Gyro"));
                Log.d(TAG, "Y added to series");
            }
            if(recording.getZ()) {
                xySeries.add(new SimpleXYSeries(parsedTimestamps, zValuesGyro, "Z Gyro"));
                Log.d(TAG, "Z added to series");
            }
        }
        if(recording.getMag()){
            if(recording.getX()) {
                xySeries.add(new SimpleXYSeries(parsedTimestamps, xValuesMag, "X Mag"));
                Log.d(TAG, "X added to series");
            }
            if(recording.getY()) {
                xySeries.add(new SimpleXYSeries(parsedTimestamps, yValuesMag, "Y Mag"));
                Log.d(TAG, "Y added to series");
            }
            if(recording.getZ()) {
                xySeries.add(new SimpleXYSeries(parsedTimestamps, zValuesMag, "Z Mag"));
                Log.d(TAG, "Z added to series");
            }
        }

    }
    private void setFormatters(){
        //generate format for each series with a random line colour
        seriesFormat.add(new LineAndPointFormatter(
                Color.rgb(255, rnd.nextInt(256), rnd.nextInt(256)), // line color random
                Color.rgb(0, 100, 0),                              // point color
                null,                                              // fill color (none)
                new PointLabelFormatter(Color.WHITE)));

    }


}
