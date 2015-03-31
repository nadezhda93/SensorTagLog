package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by nadia on 30/03/15.
 * This Activity will receive the recording that was expanded
 * in the PlotActivity and pull out the data from the database
 * and perform LDA classification using 3/4 as training and
 * 1/4 as classification data and show the results to the user
 */
public class ClassifyActivity extends Activity {
    private final String TAG = "ClassifyActivity";
    public static final String EXTRAS_REC_ID = "ExtrasRecId";

    private Intent intent = new Intent();
    private int    mRecId;
    private RecordingsDataModel recording;
    private String tableAcc, tableGyro;

    private SensorDataSQLiteHelper db    = new SensorDataSQLiteHelper(this);

    private ArrayList<Double> xAcc = new ArrayList<Double>();
    private ArrayList<Double> yAcc = new ArrayList<Double>();
    private ArrayList<Double> zAcc = new ArrayList<Double>();

    private ArrayList<Double> xGyro = new ArrayList<Double>();
    private ArrayList<Double> yGyro = new ArrayList<Double>();
    private ArrayList<Double> zGyro = new ArrayList<Double>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate started");
        setContentView(R.layout.activity_bluetooth);

        // extract intent information from RecordingsDataActivity
        intent = getIntent();
        mRecId = intent.getIntExtra(EXTRAS_REC_ID, 0);
        Log.d(TAG, "ID selected from RecordingActivity: " + mRecId);

        //get the object that corresponds to mRecId from mRecordings array in RecDataActivity
        for (int i = 0;i<RecordingsDataActivity.mRecordings.size();i++){
            if(RecordingsDataActivity.mRecordings.get(i).getId() == mRecId){
                recording = RecordingsDataActivity.mRecordings.get(i);
            }
        }

        //pull required data out of database
        getClassifyData();

        //convert all ArrayLists to primitive doubles as
        //required by LDA library
        double[] xAccDouble;
        double[] yAccDouble;
        double[] zAccDouble;


        xAccDouble = convertToDoubles(xAcc);
        yAccDouble = convertToDoubles(yAcc);
        zAccDouble = convertToDoubles(zAcc);

        //split up data into training and classification and merge into 2D array
        Log.d(TAG, "TRAINING");
        double[][] accTrain = makeTrainClassData("Train", xAccDouble, yAccDouble, zAccDouble);
        Log.d(TAG, "CLASS");
        double[][] accClass = makeTrainClassData("Class", xAccDouble, yAccDouble, zAccDouble);
        double[][] Acc = new double[xAccDouble.length][3];

        int n_accTrain = accTrain.length;

//        Log.d(TAG,"ALL DATA");
//        for (int rows = 0;rows < xAccDouble.length;rows++){
//            Acc[rows][0] = xAccDouble[rows];
//            Acc[rows][1] = yAccDouble[rows];
//            Acc[rows][2] = zAccDouble[rows];
//
//            Log.d(TAG,"row: " + rows + " : " + Acc[rows][0] + " " + Acc[rows][1] + " " + Acc[rows][2]);
//        }

        //for(int j = 0; j<xAccDouble.length; j++) {
          //  Log.d(TAG, "value orig : " + xAcc.get(j) + " value double " + xAccDouble[j]);
        //}

        //LDA EXAMPLE FROM WEBSITE
        // You need a double array with the features of the objects
        // and an int-array with their group membership
//        int[] group = { 1, 1, 1, 1, 2, 2, 2 };
//        double[][] data = { { 2.95, 6.63 }, { 2.53, 7.79 }, { 3.57, 5.65 },
//                { 3.16, 5.47 }, { 2.58, 4.46 }, { 2.16, 6.22 }, { 3.27, 3.52 } };
//
//        //The LDA is "trained"
//        LDA test = new LDA(data, group, true);
//
//        //Now we will try to classify new data
//        double[] testData = { 2.81, 5.46 };
//        Log.d(TAG, "Predicted group: " + test.predict(testData));
//
//        //Let's have a look at the values of the discriminant functions
//        double[] values = test.getDiscriminantFunctionValues(testData);
//        for(int i = 0; i < values.length; i++){
//            Log.d(TAG, "Discriminant function " + (i+1)
//                    + ": " + values[i]);
 //       }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void getClassifyData(){
        //Accelerometer - get x y z values
        tableAcc = "Accelerometer";
        xAcc = db.queryValues(mRecId, tableAcc, "x");
        //double[] x_acc = new double[xAcc.size()];
        //x_acc = (double)xAcc.toArray(x_acc);

        yAcc = db.queryValues(mRecId, tableAcc, "y");
        zAcc = db.queryValues(mRecId, tableAcc, "z");


        //Gyroscope - get x y z values
        tableGyro = "Gyroscope";
        xGyro = db.queryValues(mRecId, tableGyro, "x");
        yGyro = db.queryValues(mRecId, tableGyro, "y");
        zGyro = db.queryValues(mRecId, tableGyro, "z");
    }


    private static double[] convertToDoubles(List<Double> Doubles)
    {
        double[] doubles_primitive = new double[Doubles.size()];
        //Iterator<Float> iterator = doubles.iterator();

        for(int i = 0; i < doubles_primitive.length; i++){
            doubles_primitive[i] = Doubles.get(i).doubleValue();
        }
        return doubles_primitive;
    }





    private double[][] makeTrainClassData(String data, double[] x, double[] y, double[] z){
        int n = x.length;  //lengths of either training or class data
        double[] cut_x;
        double[] cut_y;
        double[] cut_z;

        if (data.equals("Class")) {
            //classification data, take last 1/3 of samples
            n = n / 4 + 1;                   //split up data into 1/4 or 3/4
            Log.d(TAG, "length 1/4: " + n);
            int n_start_index = (x.length*3)/4;     //index 3/4 of the way in
            cut_x = new double[n];
            cut_y = new double[n];
            cut_z = new double[n];
            Log.d(TAG, "Length cut_x: " + cut_x.length);

            for(int elem = 0; elem<n; elem++){
                cut_x[elem] = x[elem+n_start_index];
                cut_y[elem] = y[elem+n_start_index];
                cut_z[elem] = z[elem+n_start_index];

            }
        }
        else{
            //Training data, take first 3/4 of samples
            n = (n * 3)/4;
            Log.d(TAG, "length 3/4: " + n);
            cut_x = new double[n];
            cut_y = new double[n];
            cut_z = new double[n];

            for(int elem = 0; elem < n; elem++){
                cut_x[elem] = x[elem];
                cut_y[elem] = y[elem];
                cut_z[elem] = z[elem];
            }
        }

        double[][] sensorData = new double[n][3];

        for (int row = 0;row < n;row++){
            sensorData[row][0] = cut_x[row];
            sensorData[row][1] = cut_y[row];
            sensorData[row][2] = cut_z[row];
            //Log.d(TAG,"row: " + row + " : " + sensorData[row][0] + " "
            //            + sensorData[row][1] + " " + sensorData[row][2]);
        }
        return sensorData;
    }
}
