package com.lafarge.truckmix.tmstatic.utils;

/**
 * Created by Kim.Abdoul-Carime on 19/08/2015.
 */

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Manage application data
 * - interaction with database
 * - communication with app
 */
public class DataManager implements Parcelable {
    //attributes
    protected String [] truckList;
    protected DataTruck selectedTruck;
    protected String MACAddrBT;
    protected int targetSlump;

    //constructor
    public DataManager(){
        truckList=null;
        selectedTruck=null;
        MACAddrBT=null;
        targetSlump=-1;
    }

    //methods
    public void fetchTruckList() { //fetch trucks name in Database and record them in internal array

    }
    public String[] getTruckList(){ //return the internal array  truck list

        return truckList;
    }
    public void fetchSelectedTruck(String registration ){ //fetch truck detail in the database using the registration as parameter

    }
    public DataTruck getSelectedTruck(){ //return the internal selected truck
        return selectedTruck;
    }
    public void saveTruck(DataTruck truckToSave){ //save given truck in the database

    }
    public void fetchMACAddrBT(){ //fetch the Bluetooth MAC address in the database and record it in internal data

    }
    public void setMACAddrBT(String addr){ //set Bluetooth MAC address in internal data
        MACAddrBT=addr;
    }
    public String getMACAddrBT(){ //return the internal BT MAC address
        return MACAddrBT;
    }


    //Parcelable mandatory
    @Override
    public int describeContents(){
        return 0;
    }
    @Override
    public void writeToParcel(Parcel out, int flags){
        //ajouter attributs ici
        out.writeStringArray(truckList);
        out.writeValue(selectedTruck);
        out.writeString(MACAddrBT);
        out.writeInt(targetSlump);
    }

    public static final Parcelable.Creator<DataManager> CREATOR
            =new Parcelable.Creator<DataManager>(){
        public DataManager createFromParcel(Parcel in){
            return new DataManager(in);
        }
        public DataManager[] newArray(int size){
            return new DataManager[size];
        }
    };
    private DataManager(Parcel in){
        //ajouter attributs ici aussi
        in.writeStringArray(truckList);
        in.writeValue(selectedTruck);
        in.writeString(MACAddrBT);
        in.writeInt(targetSlump);
    }
}
