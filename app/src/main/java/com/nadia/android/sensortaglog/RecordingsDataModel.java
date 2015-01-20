package com.nadia.android.sensortaglog;


import java.util.UUID;

/**
 * Created by nadia on 08/12/14.
 * Class to store when a recording is completed by start and end timestamp and query the database
 */
public class RecordingsDataModel {

    private int    mId;           //unique ID for each recording
    private String mStart;        //start of recording timestamp
    private String mEnd;          //end of recording timestamp

    private Boolean mAcc;         //bool values for checkboxes
    private Boolean mGyro;
    private Boolean mMag;

    private Boolean mX;
    private Boolean mY;
    private Boolean mZ;

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
}
