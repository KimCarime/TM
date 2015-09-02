package com.lafarge.truckmix.tmstatic.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.lafarge.truckmix.tmstatic.utils.DataTruck;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim.Abdoul-Carime on 28/08/2015.
 */
public class DAOTrucks extends DAOBase {
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
    public static final String SELECT_TRUCKS_REGISTRATION="SELECT "+ "*"/*COLUMN_NAME_REGISTRATION*/ + " FROM " + TABLE_NAME_TRUCKS;



    public DAOTrucks(Context pContext) {
        super(pContext);
    }
    public void purge(){
        this.open();
        this.mDb.execSQL(DROP_TABLE_TRUCKS);
        this.mDb.execSQL(CREATE_TABLE_TRUCKS);
        this.close();
    }
    public void newTruck(DataTruck truck){ // add truck in database
        ContentValues value = new ContentValues();
        value.put(COLUMN_NAME_REGISTRATION,truck.getRegistrationID());
        value.put(COLUMN_NAME_T1,truck.getTruckParameters().T1);
        value.put(COLUMN_NAME_A11,truck.getTruckParameters().A11);
        value.put(COLUMN_NAME_A12,truck.getTruckParameters().A12);
        value.put(COLUMN_NAME_A13,truck.getTruckParameters().A13);
        value.put(COLUMN_NAME_MAGNETQUANTITY,truck.getTruckParameters().magnetQuantity);
        value.put(COLUMN_NAME_TIMEPUMP,truck.getTruckParameters().timePump);
        value.put(COLUMN_NAME_TIMEDELAYDRIVER,truck.getTruckParameters().timeDelayDriver);
        value.put(COLUMN_NAME_PULSENUMBER,truck.getTruckParameters().pulseNumber);
        value.put(COLUMN_NAME_FLOWMETERFREQUENCY,truck.getTruckParameters().flowmeterFrequency);
        value.put(COLUMN_NAME_COMMANDPUMP,truck.getTruckParameters().commandPumpMode.toString()); //PAS SUR POUR CELUI LA
        value.put(COLUMN_NAME_CALIBRATIONINPUTSENSORA,truck.getTruckParameters().calibrationInputSensorA);
        value.put(COLUMN_NAME_CALIBRATIONINPUTSENSORB,truck.getTruckParameters().calibrationInputSensorB);
        value.put(COLUMN_NAME_CALIBRATIONOUTPUTSENSORA,truck.getTruckParameters().calibrationOutputSensorA);
        value.put(COLUMN_NAME_CALIBRATIONOUTPUTSENSORB,truck.getTruckParameters().calibrationOutputSensorB);
        value.put(COLUMN_NAME_OPENINGTIMEEV1,truck.getTruckParameters().openingTimeEV1);
        value.put(COLUMN_NAME_OPENINGTIMEVA1,truck.getTruckParameters().openingTimeVA1);
        value.put(COLUMN_NAME_TOLERENCECOUNTING,truck.getTruckParameters().toleranceCounting);
        value.put(COLUMN_NAME_WAITINGDURATIONAFTERWATERADDITION,truck.getTruckParameters().waitingDurationAfterWaterAddition);
        value.put(COLUMN_NAME_MAX_DELAY_BEFORE_FLOWAGE,truck.getTruckParameters().maxDelayBeforeFlowage);
        value.put(COLUMN_NAME_MAXFLOWAGEERROR, truck.getTruckParameters().maxFlowageError);
        value.put(COLUMN_NAME_MAXCOUNTINGERROR, truck.getTruckParameters().maxCountingError);
        this.open();


        long rowId =mDb.insert(TABLE_NAME_TRUCKS, null, value);

        this.close();
    }
    public void deleteTruck(String registration){

    }
    public void editTruck(DataTruck truck){

    }
    public DataTruck fetchTruck(String registration){
        DataTruck selectedTruck=null;

        return selectedTruck;
    }
    public List<String> fetchTruckList(){
        this.open();
        List<String> _truckList=new ArrayList<String>();
        Cursor c=mDb.rawQuery(SELECT_TRUCKS_REGISTRATION, null);
  //      if (c.moveToFirst()) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                // Votre code
                String buff = c.getString(c.getColumnIndex(COLUMN_NAME_REGISTRATION));
                _truckList.add(buff);
            }
            c.close();
  //      }
        this.close();
        return _truckList;
    }

}
