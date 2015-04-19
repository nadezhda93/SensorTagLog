package com.nadia.android.sensortaglog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by nadia on 30/03/15.
 * This Activity will receive the recording that was expanded
 * in the PlotActivity and pull out the data from the database
 * and perform LDA classification using 3/4 as training and
 * 1/4 as classification data and show the results to the user
 * for accelerometer sensor only
 */
public class ClassifyActivity extends Activity {
    private final String TAG = "ClassifyActivity";
    public static final String EXTRAS_REC_ID = "ExtrasRecId";

    private Intent intent = new Intent();
    private int    mRecId;
    private String tableAcc;

    private TextView mTrueWalk, mTrueRun, mTrueJump, mTrueOverall;

    private SensorDataSQLiteHelper db    = new SensorDataSQLiteHelper(this);

    private ArrayList<Double> xAccW = new ArrayList<Double>();
    private ArrayList<Double> xAccR = new ArrayList<Double>();
    private ArrayList<Double> xAccJ = new ArrayList<Double>();

    private ArrayList<Double> yAccW = new ArrayList<Double>();
    private ArrayList<Double> yAccR = new ArrayList<Double>();
    private ArrayList<Double> yAccJ = new ArrayList<Double>();

    private ArrayList<Double> zAccW = new ArrayList<Double>();
    private ArrayList<Double> zAccR = new ArrayList<Double>();
    private ArrayList<Double> zAccJ = new ArrayList<Double>();

    private double[] xAccWDouble, xAccRDouble, xAccJDouble;
    private double[] yAccWDouble, yAccRDouble, yAccJDouble;
    private double[] zAccWDouble, zAccRDouble, zAccJDouble;

    private int[] labelsTrain;
    private int[] labelsClass;

    private int[] prediction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate started");
        setContentView(R.layout.activity_classify);

        // extract intent information from RecordingsDataActivity
        intent = getIntent();
        mRecId = intent.getIntExtra(EXTRAS_REC_ID, 0);
        Log.d(TAG, "Intent from RecordingActivity: " + mRecId);

        //set TextView widgets
        mTrueWalk    = (TextView)findViewById(R.id.true_walking);
        mTrueRun     = (TextView)findViewById(R.id.true_running);
        mTrueJump    = (TextView)findViewById(R.id.true_jumping);
        mTrueOverall = (TextView)findViewById(R.id.true_overall);

        //pull required data out of database
        getClassifyData();

        //convert all ArrayLists to primitive doubles as
        //required by LDA library
        double[] xAccDouble;
        double[] yAccDouble;
        double[] zAccDouble;


        xAccWDouble = convertToDoubles(xAccW);
        xAccRDouble = convertToDoubles(xAccR);
        xAccJDouble = convertToDoubles(xAccJ);

        yAccWDouble = convertToDoubles(yAccW);
        yAccRDouble = convertToDoubles(yAccR);
        yAccJDouble = convertToDoubles(yAccJ);

        zAccWDouble = convertToDoubles(zAccW);
        zAccRDouble = convertToDoubles(zAccR);
        zAccJDouble = convertToDoubles(zAccJ);

        //split up data into training and classification for each activity
        Log.d(TAG, "TRAINING");
        double[][] accWTrain = makeTrainClassData("Train", xAccWDouble, yAccWDouble, zAccWDouble);
        double[][] accRTrain = makeTrainClassData("Train", xAccRDouble, yAccRDouble, zAccRDouble);
        double[][] accJTrain = makeTrainClassData("Train", xAccJDouble, yAccJDouble, zAccJDouble);


        Log.d(TAG, "CLASS");
        double[][] accWClass = makeTrainClassData("Class", xAccWDouble, yAccWDouble, zAccWDouble);
        double[][] accRClass = makeTrainClassData("Class", xAccRDouble, yAccRDouble, zAccRDouble);
        double[][] accJClass = makeTrainClassData("Class", xAccJDouble, yAccJDouble, zAccJDouble);

        //merge training data: walking_train, running_train, jumping_train
        double allTrain[][] = mergeMatrices(accWTrain,accRTrain,accJTrain);

        //make labels for the training data 1/3 walking, 1/3 running, 1/3 jumping
        labelsTrain = makeLabels(allTrain);

        //merge training data: walking_class, running_class, jumping_class
        double allClass[][] = mergeMatrices(accWClass,accRClass,accJClass);

        //make known labels for comparison
        labelsClass = makeLabels(allClass);

        prediction = new int[labelsClass.length];
        //PERFORM LDA
        // You need a double array with the features of the objects
        // and an int-array with their group membership

        //The LDA is "trained"
        LDA classify = new LDA(allTrain, labelsTrain, true);

        //Now we will try to classify new data for every row
        for (int i=0; i<allClass.length; i++){
            prediction[i] = classify.predict(allClass[i]);
        }

        //Check the values of the prediction against known labels
//        for(int i = 0; i < labelsClass.length; i++){
//            Log.d(TAG, "index "+ i + " Real: " + labelsClass[i] + " Predic: " + prediction[i]);
//        }

        float[] accuracy = getAccuracy(prediction);
        Log.d(TAG, "Accuracy percentage: " + accuracy[3]*100);

        mTrueWalk.setText(String.valueOf((int)accuracy[0]) + "/350");
        mTrueRun.setText(String.valueOf((int)accuracy[1])+ "/350");
        mTrueJump.setText(String.valueOf((int)accuracy[2])+ "/350");
        mTrueOverall.setText(String.valueOf(accuracy[3]*100) + " %");

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void getClassifyData(){
        //Accelerometer - get x y z values
        //Order: 1. walking 2. jumping 3. running
        tableAcc = "Accelerometer";

        xAccW = db.queryValues(1, tableAcc, "x");
        xAccR = db.queryValues(3, tableAcc, "x");
        xAccJ = db.queryValues(2, tableAcc, "x");

        yAccW = db.queryValues(1, tableAcc, "y");
        yAccR = db.queryValues(3, tableAcc, "y");
        yAccJ = db.queryValues(2, tableAcc, "y");

        zAccW = db.queryValues(1, tableAcc, "z");
        zAccR = db.queryValues(3, tableAcc, "z");
        zAccJ = db.queryValues(2, tableAcc, "z");

    }

    private static double[] convertToDoubles(List<Double> Doubles) {
        double[] doubles_primitive = new double[Doubles.size()];
        //Iterator<Float> iterator = doubles.iterator();

        for(int i = 0; i < doubles_primitive.length; i++){
            doubles_primitive[i] = Doubles.get(i).doubleValue();
        }
        return doubles_primitive;
    }

    private double[][] makeTrainClassData(String data, double[]x, double[]y, double[]z){
        int n = x.length;  //lengths of either training or class data
        double[] cut_x;
        double[] cut_y;
        double[] cut_z;

        if (data.equals("Class")) {
            //classification data, take last 1/3 of samples
            n = 350;                          //split up data into 1/4 or 3/4
            Log.d(TAG, "length 1/4: " + n);
            int n_start_index = 1050;     //index 3/4 of the way in
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
            n = 1050;
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

    private double[][] mergeMatrices(double[][]walking, double[][]running, double[][]jumping){
       //Log.d(TAG, "Walk col: " + String.valueOf(walking[0].length) +"row: "+ walking.length);
       //Log.d(TAG, "Run col: "  + String.valueOf(running[0].length) +"row: " + running.length);
       //Log.d(TAG, "Jump col: " + String.valueOf(jumping[0].length) +"row: " + jumping.length);

        double[][] result = new double[walking.length + running.length + jumping.length][3];

        for(int w = 0; w < walking.length; w++)
            System.arraycopy( walking[w], 0, result[0], 0, walking[w].length);

        for(int r = 0; r < running.length; r++)
            System.arraycopy( running[r], 0, result[r + walking.length], 0, running[r].length );

        for(int j = 0; j < jumping.length; j++)
            System.arraycopy(jumping[j], 0, result[j + walking.length + running.length], 0, jumping[j].length);

        //Log.d(TAG, "result col: "+ result[0].length + " row: 3150/1050?? " + result.length);
        return result;
    }

    private int[] makeLabels(double[][]trainData){
        int[] labels = new int[trainData.length];
        int n = trainData.length/3;

        for(int i = 0; i<n; i++){
            labels[i]        = 1;  //walking
            labels[i+n]      = 2;  //running
            labels[i+2*n]    = 3;  //jumping
        }

        return labels;
    }

    private float[] getAccuracy(int[] prediction){
        int n = prediction.length;
        Log.d(TAG, "length pred: " + n);
        int n_class = n/3;

        int trueWalking = 0;
        int trueRunning = 0;
        int trueJumping = 0;

        //get true positives for walking from 0 to 249
        for(int i = 0; i<n_class; i++){
            if(prediction[i] == 1){
                trueWalking = trueWalking + 1;
               // Log.d(TAG, "trueWalk: " + trueWalking);
            }
        }
        //get true positives for running from 350 to 699
        for(int i = n_class; i<2*n_class; i++){
            if(prediction[i] == 2){
                trueRunning = trueRunning + 1;
                //Log.d(TAG, "trueRun: " + trueRunning);
            }
        }

        //get true positives for jumping from 700 to 1049
        for(int i = 2*n_class; i<3*n_class; i++){
            if(prediction[i] == 3){
                trueJumping = trueJumping + 1;
               // Log.d(TAG, "trueJump: " + trueJumping);
            }
        }
        float truePositives = ((float) trueWalking) + ((float)trueRunning) + ((float)trueJumping);

        float[] accuracy = new float[4];
        accuracy[0] = (float)trueWalking;
        accuracy[1] = (float)trueRunning;
        accuracy[2] = (float)trueJumping;
        accuracy[3] = truePositives/n;

        Log.d(TAG, "TruePos: " + truePositives);
        Log.d(TAG,"OverallAccuracy: " + truePositives/n);
        return accuracy;

    }
}


