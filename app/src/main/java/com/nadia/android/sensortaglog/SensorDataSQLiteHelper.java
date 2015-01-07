package com.nadia.android.sensortaglog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import java.io.File;



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
    //class constructor
    public SensorDataSQLiteHelper(Context context){
        super(context, Environment.getExternalStorageDirectory()
                + File.separator + FILE_DIR
                + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
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
        // 2. get number of last recording by query
        //recID = queryDatabase("Accelerometer");

        //increment by one if a recording exists
       // recID = recID_old + 1;
        //if no recordings, set ID to 1
        //recID = 1;
        // 3. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(RECID, recID);
        values.put(TIME, timestamp); // put timestamp
        values.put(X, x);
        values.put(Y, y);
        values.put(Z, z);
        // 4. insert into required table
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
    public int queryDatabase(String table) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + table;
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToLast();
        int ID = cursor.getInt(0);
        Log.d(TAG, "recording id = " + ID);
        return ID;
    }
}
