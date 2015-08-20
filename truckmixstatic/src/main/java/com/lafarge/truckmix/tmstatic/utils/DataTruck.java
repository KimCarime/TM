package com.lafarge.truckmix.tmstatic.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.lafarge.truckmix.common.enums.CommandPumpMode;

/**
 * Created by Kim.Abdoul-Carime on 19/08/2015.
 */
public class DataTruck implements Parcelable {

        //attribute
        private  String registrationID;
        private double T1;
        private double A11;
        private  double A12;
        private double A13;
        private  int magnetQuantity ;
        private  int timePump ;
        private  int timeDelayDriver ;
        private  int pulseNumber ;
        private  int flowmeterFrequency ;
        private  CommandPumpMode commandPumpMode ;
        private  double calibrationInputSensorA ;
        private  double calibrationInputSensorB ;
        private  double calibrationOutputSensorA;
        private  double calibrationOutputSensorB;
        private   int openingTimeEV1 ;
        private   int openingTimeVA1 ;
        private   int toleranceCounting ;
        private   int waitingDurationAfterWaterAddition ;
        private   int maxDelayBeforeFlowage ;
        private   int maxFlowageError ;

    //getter and setter
    public String getRegistrationID() {
        return registrationID;
    }
    public void setRegistrationID(String registrationID) {
        this.registrationID = registrationID;
    }

    public double getT1() {
        return T1;
    }
    public void setT1(double t1) {
        T1 = t1;
    }

    public int getMaxCountingError() {
        return maxCountingError;
    }
    public void setMaxCountingError(int maxCountingError) {
        this.maxCountingError = maxCountingError;
    }

    public double getA11() {
        return A11;
    }
    public void setA11(double a11) {
        A11 = a11;
    }

    public double getA12() {
        return A12;
    }
    public void setA12(double a12) {
        A12 = a12;
    }

    public double getA13() {
        return A13;
    }
    public void setA13(double a13) {
        A13 = a13;
    }

    public int getMagnetQuantity() {
        return magnetQuantity;
    }
    public void setMagnetQuantity(int magnetQuantity) {
        this.magnetQuantity = magnetQuantity;
    }

    public int getTimePump() {
        return timePump;
    }
    public void setTimePump(int timePump) {
        this.timePump = timePump;
    }

    public int getTimeDelayDriver() {
        return timeDelayDriver;
    }
    public void setTimeDelayDriver(int timeDelayDriver) {
        this.timeDelayDriver = timeDelayDriver;
    }

    public int getPulseNumber() {
        return pulseNumber;
    }
    public void setPulseNumber(int pulseNumber) {
        this.pulseNumber = pulseNumber;
    }

    public int getFlowmeterFrequency() {
        return flowmeterFrequency;
    }
    public void setFlowmeterFrequency(int flowmeterFrequency) {
        this.flowmeterFrequency = flowmeterFrequency;
    }

    public CommandPumpMode getCommandPumpMode() {
        return commandPumpMode;
    }
    public void setCommandPumpMode(CommandPumpMode commandPumpMode) {
        this.commandPumpMode = commandPumpMode;
    }

    public double getCalibrationInputSensorA() {
        return calibrationInputSensorA;
    }
    public void setCalibrationInputSensorA(double calibrationInputSensorA) {
        this.calibrationInputSensorA = calibrationInputSensorA;
    }

    public double getCalibrationInputSensorB() {
        return calibrationInputSensorB;
    }
    public void setCalibrationInputSensorB(double calibrationInputSensorB) {
        this.calibrationInputSensorB = calibrationInputSensorB;
    }

    public double getCalibrationOutputSensorA() {
        return calibrationOutputSensorA;
    }
    public void setCalibrationOutputSensorA(double calibrationOutputSensorA) {
        this.calibrationOutputSensorA = calibrationOutputSensorA;
    }

    public double getCalibrationOutputSensorB() {
        return calibrationOutputSensorB;
    }
    public void setCalibrationOutputSensorB(double calibrationOutputSensorB) {
        this.calibrationOutputSensorB = calibrationOutputSensorB;
    }

    public int getOpeningTimeEV1() {
        return openingTimeEV1;
    }
    public void setOpeningTimeEV1(int openingTimeEV1) {
        this.openingTimeEV1 = openingTimeEV1;
    }

    public int getOpeningTimeVA1() {
        return openingTimeVA1;
    }
    public void setOpeningTimeVA1(int openingTimeVA1) {
        this.openingTimeVA1 = openingTimeVA1;
    }

    public int getToleranceCounting() {
        return toleranceCounting;
    }
    public void setToleranceCounting(int toleranceCounting) {
        this.toleranceCounting = toleranceCounting;
    }

    public int getWaitingDurationAfterWaterAddition() {
        return waitingDurationAfterWaterAddition;
    }
    public void setWaitingDurationAfterWaterAddition(int waitingDurationAfterWaterAddition) {
        this.waitingDurationAfterWaterAddition = waitingDurationAfterWaterAddition;
    }

    public int getMaxDelayBeforeFlowage() {
        return maxDelayBeforeFlowage;
    }
    public void setMaxDelayBeforeFlowage(int maxDelayBeforeFlowage) {
        this.maxDelayBeforeFlowage = maxDelayBeforeFlowage;
    }

    public int getMaxFlowageError() {
        return maxFlowageError;
    }
    public void setMaxFlowageError(int maxFlowageError) {
        this.maxFlowageError = maxFlowageError;
    }

    private   int maxCountingError;

        //constructor
        public DataTruck(){ //constructor for new truck with default values
            registrationID="NEW_TRUCK";
            T1=3.4563f;
            A11=563.376f;
            A12=39.844f;
            A13=4.3254f;
            magnetQuantity=24 ;
            timePump =15;
            timeDelayDriver =120;
            pulseNumber=45 ;
            flowmeterFrequency=60 ;
            commandPumpMode=CommandPumpMode.SEMI_AUTO ;
            calibrationInputSensorA=2.5f ;
            calibrationInputSensorB=2.5f ;
            calibrationOutputSensorA=0;
            calibrationOutputSensorB=0;
            openingTimeEV1=3 ;
            openingTimeVA1=180 ;
            toleranceCounting=10 ;
            waitingDurationAfterWaterAddition=90 ;
            maxDelayBeforeFlowage=64 ;
            maxFlowageError=5 ;
            maxCountingError=6;
        }
    public DataTruck(DataTruck truck){
        registrationID=truck.registrationID;
        T1=truck.T1;
        A11=truck.A11;
        A12=truck.A12;
        A13=truck.A13;
        magnetQuantity=truck.magnetQuantity;
        timePump =truck.timePump;
        timeDelayDriver =truck.timeDelayDriver;
        pulseNumber=truck.pulseNumber;
        flowmeterFrequency=truck.flowmeterFrequency ;
        commandPumpMode=truck.commandPumpMode ;
        calibrationInputSensorA=truck.calibrationInputSensorA ;
        calibrationInputSensorB=truck.calibrationInputSensorB;
        calibrationOutputSensorA=truck.calibrationOutputSensorA;
        calibrationOutputSensorB=truck.calibrationOutputSensorB;
        openingTimeEV1=truck.openingTimeEV1 ;
        openingTimeVA1=truck.openingTimeVA1 ;
        toleranceCounting=truck.toleranceCounting ;
        waitingDurationAfterWaterAddition=truck.waitingDurationAfterWaterAddition ;
        maxDelayBeforeFlowage=truck.maxDelayBeforeFlowage;
        maxFlowageError=truck.maxFlowageError ;
        maxCountingError=truck.maxCountingError;
    }
    //Parcelable mandatory
    @Override
    public int describeContents(){
        return 0;
    }
    @Override
    public void writeToParcel(Parcel out, int flags){
        //ajouter attributs ici
        out.writeString(registrationID);
        out.writeDouble(T1);
        out.writeDouble(A11);
        out.writeDouble(A12);
        out.writeDouble(A13);
        out.writeInt(magnetQuantity);
        out.writeInt(timePump);
        out.writeInt(timeDelayDriver);
        out.writeInt(pulseNumber);
        out.writeInt(flowmeterFrequency) ;
        out.writeValue(commandPumpMode);
        out.writeDouble(calibrationInputSensorA) ;
        out.writeDouble(calibrationInputSensorB);
        out.writeDouble(calibrationOutputSensorA);
        out.writeDouble(calibrationOutputSensorB);
        out.writeInt(openingTimeEV1);
        out.writeInt(openingTimeVA1);
        out.writeInt(toleranceCounting);
        out.writeInt(waitingDurationAfterWaterAddition);
        out.writeInt(maxDelayBeforeFlowage);
        out.writeInt(maxFlowageError) ;
    }

    public static final Parcelable.Creator<DataTruck> CREATOR
            =new Parcelable.Creator<DataTruck>(){
        public DataTruck createFromParcel(Parcel in){
            return new DataTruck(in);
        }
        public DataTruck[] newArray(int size){
            return new DataTruck[size];
        }
    };
    private DataTruck(Parcel in){
        //ajouter attributs ici aussi
        in.writeString(registrationID);
        in.writeDouble(T1);
        in.writeDouble(A11);
        in.writeDouble(A12);
        in.writeDouble(A13);
        in.writeInt(magnetQuantity);
        in.writeInt(timePump);
        in.writeInt(timeDelayDriver);
        in.writeInt(pulseNumber);
        in.writeInt(flowmeterFrequency) ;
        in.writeValue(commandPumpMode);
        in.writeDouble(calibrationInputSensorA) ;
        in.writeDouble(calibrationInputSensorB);
        in.writeDouble(calibrationOutputSensorA);
        in.writeDouble(calibrationOutputSensorB);
        in.writeInt(openingTimeEV1);
        in.writeInt(openingTimeVA1);
        in.writeInt(toleranceCounting);
        in.writeInt(waitingDurationAfterWaterAddition);
        in.writeInt(maxDelayBeforeFlowage);
        in.writeInt(maxFlowageError) ;
    }

}
