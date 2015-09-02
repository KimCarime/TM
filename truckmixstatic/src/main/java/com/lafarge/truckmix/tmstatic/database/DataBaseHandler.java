package com.lafarge.truckmix.tmstatic.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Kim.Abdoul-Carime on 28/08/2015.
 */
public class DataBaseHandler extends SQLiteOpenHelper {
    //tables
    public static final String TABLE_NAME_TRUCKS="Truck";

    //columns
    public static final String COLUMN_NAME_KEY="id";
    public static final String COLUMN_NAME_REGISTRATION="registration";
    public static final String COLUMN_NAME_T1="T1";
    public static final String COLUMN_NAME_A11="A11";
    public static final String COLUMN_NAME_A12="A12";
    public static final String COLUMN_NAME_A13="A13";
    public static final String COLUMN_NAME_MAGNETQUANTITY="magnet_quantity";
    public static final String COLUMN_NAME_TIMEPUMP="time_pump";
    public static final String COLUMN_NAME_TIMEDELAYDRIVER="time_delay_driver";
    public static final String COLUMN_NAME_PULSENUMBER="pulse_number";
    public static final String COLUMN_NAME_FLOWMETERFREQUENCY="flowmeter_frequency";
    public static final String COLUMN_NAME_COMMANDPUMP="command_pump";
    public static final String COLUMN_NAME_CALIBRATIONINPUTSENSORA="calibration_input_sensor_a";
    public static final String COLUMN_NAME_CALIBRATIONINPUTSENSORB="calibration_input_sensor_b";
    public static final String COLUMN_NAME_CALIBRATIONOUTPUTSENSORA="calibration_output_sensor_a";
    public static final String COLUMN_NAME_CALIBRATIONOUTPUTSENSORB="calibration_output_sensor_b";
    public static final String COLUMN_NAME_OPENINGTIMEEV1="opening_time_ev1";
    public static final String COLUMN_NAME_OPENINGTIMEVA1="opening_time_va1";
    public static final String COLUMN_NAME_TOLERENCECOUNTING="tolerence_counting";
    public static final String COLUMN_NAME_WAITINGDURATIONAFTERWATERADDITION="waiting_duration_after_water_addition";
    public static final String COLUMN_NAME_MAX_DELAY_BEFORE_FLOWAGE="max_delay_before_flowage";
    public static final String COLUMN_NAME_MAXFLOWAGEERROR="max_flowage_error";
    public static final String COLUMN_NAME_MAXCOUNTINGERROR="max_counting_error";
    //requests
    public static final String CREATE_TABLE_TRUCKS="CREATE TABLE "+ TABLE_NAME_TRUCKS + " (" +
            COLUMN_NAME_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME_REGISTRATION + " TEXT NOT NULL UNIQUE, "+
            COLUMN_NAME_T1 + " REAL NOT NULL, "+
            COLUMN_NAME_A11 + " REAL NOT NULL, "+
            COLUMN_NAME_A12 + " REAL NOT NULL, "+
            COLUMN_NAME_A13 + " REAL NOT NULL, "+
            COLUMN_NAME_MAGNETQUANTITY + " INTEGER NOT NULL, " +
            COLUMN_NAME_TIMEPUMP + " INTEGER NOT NULL, " +
            COLUMN_NAME_TIMEDELAYDRIVER + " INTEGER NOT NULL, " +
            COLUMN_NAME_PULSENUMBER + " INTEGER NOT NULL, " +
            COLUMN_NAME_FLOWMETERFREQUENCY + " INTEGER NOT NULL, " +
            COLUMN_NAME_COMMANDPUMP + " TEXT NOT NULL, " +
            COLUMN_NAME_CALIBRATIONINPUTSENSORA + " REAL NOT NULL, "+
            COLUMN_NAME_CALIBRATIONINPUTSENSORB + " REAL NOT NULL, "+
            COLUMN_NAME_CALIBRATIONOUTPUTSENSORA + " REAL NOT NULL, "+
            COLUMN_NAME_CALIBRATIONOUTPUTSENSORB + " REAL NOT NULL, "+
            COLUMN_NAME_OPENINGTIMEEV1 + " INTEGER NOT NULL, " +
            COLUMN_NAME_OPENINGTIMEVA1 + " INTEGER NOT NULL, " +
            COLUMN_NAME_TOLERENCECOUNTING + " INTEGER NOT NULL, " +
            COLUMN_NAME_WAITINGDURATIONAFTERWATERADDITION + " INTEGER NOT NULL, " +
            COLUMN_NAME_MAX_DELAY_BEFORE_FLOWAGE + " INTEGER NOT NULL, " +
            COLUMN_NAME_MAXFLOWAGEERROR + " INTEGER NOT NULL, " +
            COLUMN_NAME_MAXCOUNTINGERROR + " INTEGER NOT NULL); ";
    public static final String DROP_TABLE_TRUCKS="DROP TABLE IF EXISTS " + TABLE_NAME_TRUCKS + ";";


    //constructor
    public DataBaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    //methods
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TRUCKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_TRUCKS);
        onCreate(db);
    }
    public void purge(SQLiteDatabase db){
        db.execSQL(DROP_TABLE_TRUCKS);
        onCreate(db);
    }
}
