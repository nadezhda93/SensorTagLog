package com.nadia.android.sensortaglog;


import java.util.UUID;

/**
 * Created by nadia on 08/12/14.
 * Class to store when a recording is completed by start and end timestamp and query the database
 */
public class RecordingsDataModel {

    private UUID mId;             //unique ID for each recording
    private String mTitle;        //title of recording
    private String mStart;        //start of recording timestamp
    private String mEnd;          //end of recording timestamp

    //constructor
    public RecordingsDataModel(String title, String start, String end){
        mId = UUID.randomUUID();        //generate a unique number for recording
        mTitle = title;
        mStart = start;
        mEnd = end;

    }

    //getters and setters for member functions
    public UUID getId(){
        return mId;
    }

    public void setTitle(String title){
        mTitle = title;
    }

    public String getTitle(){
        return mTitle;
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
