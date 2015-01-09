package com.nadia.android.sensortaglog;


import java.util.UUID;

/**
 * Created by nadia on 08/12/14.
 * Class to store when a recording is completed by start and end timestamp and query the database
 */
public class RecordingsDataModel {

    private int mId;              //unique ID for each recording
    private String mStart;        //start of recording timestamp
    private String mEnd;          //end of recording timestamp


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
}
