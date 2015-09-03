package com.lafarge.truckmix.tmstatic.utils;



import com.lafarge.truckmix.common.enums.CommandPumpMode;
import com.lafarge.truckmix.common.models.TruckParameters;

import java.io.Serializable;

/**
 * Created by Kim.Abdoul-Carime on 19/08/2015.
 */
public class DataTruck implements Serializable {

        //attribute
        private  String registrationID;
        private TruckParameters parameters;

    //getter and setter
    public String getRegistrationID() {
        return registrationID;
    }
    public void setRegistrationID(String registrationID) {
        this.registrationID = registrationID;
    }

    //methods
    public TruckParameters getTruckParameters() {
        return parameters;
    }
    public void setTruckParameters(TruckParameters parameters){
        if (parameters == null) return;

        this.parameters=parameters;
    }

    //constructor
        public DataTruck(){ //constructor for new truck with default values
            registrationID="NEW_TRUCK";
            double T1=3.4563f;
            double A11=563.376f;
            double A12=39.844f;
            double A13=4.3254f;
            int magnetQuantity=24 ;
            int timePump =15;
            int timeDelayDriver =5;
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
            parameters=new TruckParameters(T1, A11, A12, A13, magnetQuantity, timePump, timeDelayDriver, pulseNumber,
                      flowmeterFrequency, commandPumpMode, calibrationInputSensorA, calibrationInputSensorB, calibrationOutputSensorA,
                    calibrationOutputSensorB, openingTimeEV1, openingTimeVA1, toleranceCounting, waitingDurationAfterWaterAddition, maxDelayBeforeFlowage,
                    maxFlowageError, maxCountingError);

        }
    public DataTruck(DataTruck truck){
        this.registrationID=truck.registrationID;
        this.parameters=truck.parameters;

    }
    public DataTruck(String registration,TruckParameters param){
        this.registrationID=registration;
        this.parameters=param;

    }

}
