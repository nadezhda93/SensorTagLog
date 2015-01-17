package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.androidplot.Plot;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;

import org.w3c.dom.Text;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by nadia on 11/01/15.
 * Activity which will query the database for the values of the
 * sensors and plot them on a graph from AndroidPlot API library
 */
public class PlotActivity extends Activity {

    private final String TAG = "PlotActivity";

    public static final String EXTRAS_REC_ID          = "ExtrasRecId";
    public static final String EXTRAS_START_TIMESTAMP = "ExtrasStartTimestamp";
    public static final String EXTRAS_END_TIMESTAMP   = "ExtrasEndTimestamp";

    private Intent intent = new Intent();

    private int    mRecId;
    private String mRecStartTimestamp;
    private String mRecEndTimestamp;

    private XYPlot plot;

    private SensorDataSQLiteHelper db    = new SensorDataSQLiteHelper(this);
    private ArrayList<String> timestamps = new ArrayList<String>();
    private SimpleDateFormat sdf         = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");

    private ArrayList<Long> parsedTimestamps = new ArrayList<Long>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);

        // extract intent information from RecordingsDataActivity
        intent = getIntent();
        mRecId = intent.getIntExtra(EXTRAS_REC_ID, 0);
        mRecStartTimestamp = intent.getStringExtra(EXTRAS_START_TIMESTAMP);
        mRecEndTimestamp = intent.getStringExtra(EXTRAS_END_TIMESTAMP);
        Log.d(TAG, "ID: " + mRecId + " Start: " + mRecStartTimestamp + " End: " + mRecEndTimestamp);

        //x values (Accelerometer timestamps)
        timestamps = db.queryTimestamps(mRecId);
        parsedTimestamps = parseTimestamps(timestamps);


        //initialise XYPlot reference:
        plot = (XYPlot)findViewById(R.id.mySimpleXYPlot);

        // Create a couple arrays of y-values to plot:
        Number[] series1Numbers = {1, 8, 5, 2};
        //Number[] series2Numbers = {4, 6, 3, 8, 2, 10};


        // Turn the arrays into XYSeries:
        // SimpleXYSeries takes a List so turn array into a List
        // Y_VALS_ONLY means use the element index as the x value
        // Set the display title of the series as Series1
        XYSeries series1 = new SimpleXYSeries(parsedTimestamps,
                                               Arrays.asList(series1Numbers),
                                               "Series1");
        plot.setDomainLabel("Time");
        plot.setRangeLabel("Range");
        plot.setDomainStep(XYStepMode.SUBDIVIDE, parsedTimestamps.size());

        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getApplicationContext(), R.xml.line_point_formatter_with_plf1);

        plot.setDomainValueFormat(new Format() {

            // create a simple date format that draws on the year portion of our timestamp.
            // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
            // for a full description of SimpleDateFormat.

            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

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


        // add a new series to the xyplot:
        plot.addSeries(series1, series1Format);

//        LineAndPointFormatter series2Format = new LineAndPointFormatter();
//        series2Format.setPointLabelFormatter(new PointLabelFormatter());
//        series2Format.configure(getApplicationContext(), R.xml.line_point_formatter_with_plf2);
//        plot.addSeries(series2, series2Format);

        // reduce the number of range labels
        plot.setTicksPerRangeLabel(4);
        plot.getGraphWidget().setDomainLabelOrientation(0);
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
