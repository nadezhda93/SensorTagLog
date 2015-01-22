package com.nadia.android.sensortaglog;

import android.util.Log;

/**
 * Created by nadia on 08/12/14.
 * Class to store when a recording is completed by start and end timestamp and query the database
 * and also what attributes has the user chosen to plot
 */
public class RecordingsDataModel {
    private String TAG = "RecordingsDataModel";

    private int    mId;           //unique ID for each recording
    private String mStart;        //start of recording timestamp
    private String mEnd;          //end of recording timestamp

    private Boolean mAcc  = false;         //bool values for checkboxes
    private Boolean mGyro = false;         //init to false
    private Boolean mMag  = false;

    private Boolean mX = false;
    private Boolean mY = false;
    private Boolean mZ = false;

    //constructor
    public RecordingsDataModel(int recId, String start, String end){
        mId = recId;
        mStart = start;
        mEnd = end;

    }

    //getters and setters for member functions
    public int getId(){
        return mId;
    }

    public void setStart(String start){
        mStart = start;
    }

    public String getStart(){
        return mStart;
    }

    public void setEnd(String end){
        mEnd = end;
    }

    public String getEnd(){
        return mEnd;
    }

    public void setAcc(Boolean value){mAcc = value;}

    public Boolean getAcc(){return mAcc;}

    public void setGyro(Boolean value){mGyro = value;}

    public Boolean getGyro(){return mGyro;}

    public void setMag(Boolean value){mMag = value;}

    public Boolean getMag(){return mMag;}

    public void setX(Boolean x){mX = x;}

    public Boolean getX(){return mX;}

    public void setY(Boolean y){mY = y;}

    public Boolean getY(){return mY;}

    public void setZ(Boolean z){mZ = z;}

    public Boolean getZ(){return mZ;}

    //other member function

    //set and reset chosen atributes from checkboxes in RecordingsExpListAdapter
    public void makeSelection(RecordingsDataModel rec, String child){
        //check what the child is, set member in Model class
        if(child.equals("Accelerometer")){
            rec.setAcc(true);
        }
        else if(child.equals("Gyroscope")){
            rec.setGyro(true);
        }
        else if(child.equals("Magnetometer")){
            rec.setMag(true);
        }
        else if(child.equals("X axis")){
            rec.setX(true);
        }
        else if(child.equals("Y axis")){
            rec.setY(true);
        }
        else if(child.equals("Z axis")){
            rec.setZ(true);
        }
        Log.d(TAG, "Selected ACC " + rec.getAcc() + " GYRO "
                + rec.getGyro() + " MAG " + rec.getMag());
        Log.d(TAG, "Selected X " + rec.getX() + " Y " + rec.getY()
                + " Z " + rec.getZ());
    }

    public void clearSelection(RecordingsDataModel rec, String child){
        //check what the child is, set member in Model class
        if(child.equals("Accelerometer")){
            rec.setAcc(false);
        }
        else if(child.equals("Gyroscope")){
            rec.setGyro(false);
        }
        else if(child.equals("Magnetometer")){
            rec.setMag(false);
        }
        else if(child.equals("X axis")){
            rec.setX(false);
        }
        else if(child.equals("Y axis")){
            rec.setY(false);
        }
        else if(child.equals("Z axis")){
            rec.setZ(false);
        }
        Log.d(TAG, "Selected ACC " + rec.getAcc() + " GYRO "
                + rec.getGyro() + " MAG " + rec.getMag());
        Log.d(TAG, "Selected X " + rec.getX() + " Y " + rec.getY()
                + " Z " + rec.getZ());
    }
}
