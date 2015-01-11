package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;
import java.util.Arrays;

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


        // initialise XYPlot reference:
        plot = (XYPlot)findViewById(R.id.mySimpleXYPlot);

        // Create a couple arrays of y-values to plot:
        Number[] series1Numbers = {1, 8, 5, 2, 7, 4};
        Number[] series2Numbers = {4, 6, 3, 8, 2, 10};

        // Turn the arrays into XYSeries:
        // SimpleXYSeries takes a List so turn array into a List
        // Y_VALS_ONLY means use the element index as the x value
        // Set the display title of the series as Series1
        XYSeries series1 = new SimpleXYSeries(Arrays.asList(series1Numbers),
                                            SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                                            "Series1");

        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers),
                                            SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                                            "Series2");

        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getApplicationContext(), R.xml.line_point_formatter_with_plf1);
        // add a new series to the xyplot:
        plot.addSeries(series1, series1Format);

        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(getApplicationContext(), R.xml.line_point_formatter_with_plf2);
        plot.addSeries(series2, series2Format);

        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);
        plot.getGraphWidget().setDomainLabelOrientation(-45);
    }
}
