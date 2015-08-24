package com.lafarge.truckmix.tmstatic.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.lafarge.truckmix.common.enums.CommandPumpMode;
import com.lafarge.truckmix.common.models.TruckParameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim.Abdoul-Carime on 19/08/2015.
 */
public class DataManagerMock extends DataManager implements Serializable {
    private DataTruck mockTruck1;
    private DataTruck mockTruck2;
    private DataTruck mockTruck3;
    private String mockAddress;
    private List<String> mockList;


    public DataManagerMock(){
        super();
        mockList=new ArrayList<String>();
        mockList.add("AZERTY");
        mockList.add("TEST123");
        mockAddress="00:12:6F:35:7E:70";
        mockTruck1=new DataTruck();
        mockTruck1.setRegistrationID("AZERTY");
        mockTruck2=new DataTruck();
        mockTruck2.setRegistrationID("TEST123");
        double T1=1.4563f;
        double A11=2.376f;
        double A12=3.844f;
        double A13=4.3254f;
        int magnetQuantity=12 ;
        int timePump =15;
        int timeDelayDriver =120;
        int pulseNumber=45 ;
        int flowmeterFrequency=60 ;
        CommandPumpMode commandPumpMode=CommandPumpMode.SEMI_AUTO ;
        double calibrationInputSensorA=2.5f ;
        double calibrationInputSensorB=2.5f ;
        double calibrationOutputSensorA=0;
        double calibrationOutputSensorB=0;
        int openingTimeEV1=3 ;
        int openingTimeVA1=180 ;
        int toleranceCounting=10 ;
        int waitingDurationAfterWaterAddition=90 ;
        int maxDelayBeforeFlowage=64 ;
        int maxFlowageError=5 ;
        int maxCountingError=6;
        mockTruck2.setTruckParameters(new TruckParameters(T1, A11, A12, A13, magnetQuantity, timePump, timeDelayDriver, pulseNumber,
                flowmeterFrequency, commandPumpMode, calibrationInputSensorA, calibrationInputSensorB, calibrationOutputSensorA, calibrationOutputSensorB, openingTimeEV1, openingTimeVA1, toleranceCounting, waitingDurationAfterWaterAddition, maxDelayBeforeFlowage, maxFlowageError, maxCountingError));
    }

    public void fetchTruckList(){

        truckList=mockList;
    }
    public void fetchSelectedTruck(String registration){
        if(registration==null)
            return;
        if( registration=="AZERTY")
            selectedTruck=mockTruck1;
        else if( registration=="TEST123")
            selectedTruck=mockTruck2;
        else selectedTruck=mockTruck3;
    }
    public void saveTruck(){ //simulate record in database
        mockTruck3=this.selectedTruck;
        mockList.add(selectedTruck.getRegistrationID());
    }
    public void fetchMACAddrBT(){
        MACAddrBT=mockAddress;
    }


}
