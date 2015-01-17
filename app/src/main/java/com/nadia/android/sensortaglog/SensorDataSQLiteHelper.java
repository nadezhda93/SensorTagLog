package com.nadia.android.sensortaglog;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


/**
 * Created by nadia on 01/12/14. Database to store the broadcasted results of the
 * Accelerometer, gyro and mag in a table format with a timestamp.
 * Currently saves db file in system storage.
 */
public class SensorDataSQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG           = "SensorDataSQLiteHelper";
    private static final String DATABASE_NAME = "SensorTagDataBase";
    private static final int DATABASE_VERSION = 1;
    private static final String FILE_DIR      = "SensorTagLog";

    //table names
    private static final String TABLE_ACCELEROMETER  = "Accelerometer";
    private static final String TABLE_GYROSCOPE      = "Gyroscope";
    private static final String TABLE_MAGNETOMETER   = "Magnetometer";

    //table column names
    private static final String RECID  = "recID";
    private static final String TIME = "time";
    private static final String X    = "x";
    private static final String Y    = "y";
    private static final String Z    = "z";

    private static final String[] COLUMNS = {RECID,TIME,X,Y,Z}; //for query

    private static final String CREATE_ACCELEROMETER_TABLE = "CREATE TABLE Accelerometer ( " +
                                                              "recID INTEGER,"    +
                                                              "time TEXT, " +
                                                              "x REAL, "+
                                                              "y REAL, "+
                                                              "z REAL)";

    private static final String CREATE_GYROSCOPE_TABLE = "CREATE TABLE Gyroscope ( " +
                                                              "recID INTEGER,"    +
                                                              "time TEXT, " +
                                                              "x REAL, "+
                                                              "y REAL, "+
                                                              "z REAL)";

    private static final String CREATE_MAGNETOMETER_TABLE = "CREATE TABLE Magnetometer ( " +
                                                              "recID INTEGER,"    +
                                                              "time TEXT, " +
                                                              "x REAL, "+
                                                              "y REAL, "+
                                                              "z REAL)";

    private Context context;


    //class constructor
    public SensorDataSQLiteHelper(Context context){
        super(context, Environment.getExternalStorageDirectory()
                + File.separator + FILE_DIR
                + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        Log.d(TAG, "Database created");
    }

    @Override
    //creation and initial population of tables occurs
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_ACCELEROMETER_TABLE);
        database.execSQL(CREATE_GYROSCOPE_TABLE);
        database.execSQL(CREATE_MAGNETOMETER_TABLE);
    }

    //upgrading the database to a newer version
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        // drop older tables if existed
        database.execSQL("DROP TABLE IF EXISTS Accelerometer");
        database.execSQL("DROP TABLE IF EXISTS Gyroscope");
        database.execSQL("DROP TABLE IF EXISTS Magnetometer");
        // create fresh tables
        this.onCreate(database);
    }


    //add new accelerometer entry to table
    public void addToDatabaseTable(int recID, float x, float y, float z, String timestamp, String table){
        Log.d(TAG, "addToDatabaseTable called");

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(RECID, recID);
        values.put(TIME, timestamp); // put timestamp
        values.put(X, x);
        values.put(Y, y);
        values.put(Z, z);
        // 3. insert into required table
        if(table.equals(SensorDataActivity.ACCELEROMETER_INTENT_FILTER)) {
            db.insert(TABLE_ACCELEROMETER, // table
                    null,    //nullColumnHack
                    values); // key/value -> keys = column names/ values = column values
        }
        else if (table.equals(SensorDataActivity.GYROSCOPE_INTENT_FILTER)){
            db.insert(TABLE_GYROSCOPE, // table
                    null,    //nullColumnHack
                    values); // key/value -> keys = column names/ values = column values

        }
        else if(table.equals(SensorDataActivity.MAGNETOMETER_INTENT_FILTER)){
            db.insert(TABLE_MAGNETOMETER, // table
                    null,    //nullColumnHack
                    values); // key/value -> keys = column names/ values = column values
        }
        else{
            Log.d(TAG, "Error finding table required");
        }
        // 4. close the database
        db.close();
    }

    //query database for last entry recID
    public int queryDatabase() {
        SQLiteDatabase db_read = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM Accelerometer";
        Cursor cursor = db_read.rawQuery(selectQuery, null);
        cursor.moveToLast();
        int ID = cursor.getInt(0);
        Log.d(TAG, "recording id = " + ID);
        return ID;

    }
    //query for all recordings made and set each to an object in RecordingsDataModel
    public ArrayList queryRecordings(){
        //find out what number is the last recording
        int lastRecId = queryDatabase();
        SQLiteDatabase db_read = this.getReadableDatabase();

        //create an array to store objects
        ArrayList<RecordingsDataModel> recordings =
                new ArrayList<RecordingsDataModel>(lastRecId-1);

        //cycle through the recordings by id
        for (int i = 1 ; i < lastRecId+1; i++){
            //1. get first row of the recording and get info
            String sql = "SELECT time FROM Accelerometer WHERE recID=" + i;
            Cursor cursor = db_read.rawQuery(sql, null);
            cursor.moveToFirst();
            String t1 = cursor.getString(0);
            //Log.d(TAG, "Timestamp first: " + t1 + "rec " + i);
            //2. get last row of recording and get info
            cursor.moveToLast();
            String t2 = cursor.getString(0);
            //Log.d(TAG, "Timestamp last: " + t2 + "rec " + i);

            //3. create object with info from 1. and 2. append to array
            RecordingsDataModel rec = new RecordingsDataModel(i, t1, t2);
            recordings.add(rec);
        }
        return recordings;
    }

    public ArrayList<String> queryTimestamps(int recId){
        SQLiteDatabase db_read = this.getReadableDatabase();
        //create an array to store timestamps
        ArrayList<String> timestamps = new ArrayList<String>();
        //query string
        String sql = "SELECT time FROM Accelerometer WHERE recID=" + recId;
        Cursor cursor = db_read.rawQuery(sql, null);
        int rows = cursor.getCount(); //get number of rows
        Log.d(TAG, "Rows in rec: " + rows);
        cursor.moveToFirst();

        //go through all rows in cursor
        for (int i = 0; i<rows; i++){
            String time = cursor.getString(0);
            Log.d(TAG, "Timestamp " + i + " = " + time);
            timestamps.add(time);
            cursor.moveToNext();
        }
        return timestamps;
    }

    public boolean doesDatabaseExist() {
        File dbFile = this.context.getDatabasePath(Environment.getExternalStorageDirectory()
                + File.separator + FILE_DIR
                + File.separator + DATABASE_NAME);
        Log.d(TAG, "Does database exist? " + dbFile.exists());
        return dbFile.exists();
    }
}
