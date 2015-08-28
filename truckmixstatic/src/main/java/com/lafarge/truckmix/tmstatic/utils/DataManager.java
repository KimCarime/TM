package com.lafarge.truckmix.tmstatic.utils;

/**
 * Created by Kim.Abdoul-Carime on 19/08/2015.
 */

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Manage application data
 * - interaction with database
 * - communication with app
 */
public class DataManager implements Serializable {
    //attributes
    protected List<String> truckList;
    protected DataTruck selectedTruck;
    protected String MACAddrBT;
    protected String volumeLoad;
    protected String targetSlump;

    //constructor
    public DataManager(){
        truckList=new ArrayList<String>();
        selectedTruck=null;
        MACAddrBT=null;
        targetSlump=null;
    }

    //methods
    public void fetchTruckList(List<String> DBtruckList) { //fetch trucks name in Database and record them in internal array
        truckList=DBtruckList;
    }
    public List <String> getTruckList(){ //return the internal array  truck list

        return truckList;
    }
    public void fetchSelectedTruck(DataTruck DBtruck ){ //fetch truck detail in the database using the registration as parameter
        selectedTruck=DBtruck;
    }
    public void newTruck(){
        this.selectedTruck=new DataTruck();
    }
    public DataTruck getSelectedTruck(){ //return the internal selected truck
        return selectedTruck;
    }
    public void saveTruck(){ //save given truck in the database

    }
    public void deleteTruck(String truckToDelete){

    }

    public void setMACAddrBT(String addr){ //set Bluetooth MAC address in internal data
        MACAddrBT=addr;
    }
    public String getMACAddrBT(){ //return the internal BT MAC address
        return MACAddrBT;
    }

    public String getTargetSlump() {
        return targetSlump;
    }
    public void setTargetSlump(String targetSlump) {
        this.targetSlump = targetSlump;
    }

    public String getVolumeLoad() {
        return volumeLoad;
    }
    public void setVolumeLoad(String volumeLoad) {
        this.volumeLoad = volumeLoad;
    }


}
