package com.nadia.android.sensortaglog;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by nadia on 01/12/14.
 */
public class SensorDataSQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = "SensorDataSQLiteHelper";
    private static final String DATABASE_NAME = "SensorTagDataBase";
    private static final int DATABASE_VERSION = 1;

    private static final String FILE_DIR = "SensorTagLog";


    //Acceleration table name
    private static final String TABLE_ACCELERATION = "Acceleration";

    //Acceleration table columns


    private static final String TIME = "time";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";

    private static final String[] COLUMNS = {TIME,X,Y,Z};

    private static final String CREATE_ACCELERATION_TABLE = "CREATE TABLE Acceleration ( " +
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
        database.execSQL(CREATE_ACCELERATION_TABLE);
    }

    //upgrading the database to a newer version
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        // Drop older Acceleration table if existed
        database.execSQL("DROP TABLE IF EXISTS Acceleration");

        // create fresh books table
        this.onCreate(database);

    }


    //add new accelerometer entry to table
    public void addAcceleration(double x, double y, double z){
        Log.d(TAG, "addAcceleration called");

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
        String timestamp = dateFormat.format(new Date());

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(TIME, timestamp); // put timestamp
        values.put(X, x);
        values.put(Y, y);
        values.put(Z, z);

        // 3. insert
        db.insert(TABLE_ACCELERATION, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }
}
