package com.lafarge.truckmix.tmstatic.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kim.Abdoul-Carime on 19/08/2015.
 */
public class DataManagerMock extends DataManager implements Parcelable {
    private DataTruck mockTruck1;
    private DataTruck mockTruck2;
    private DataTruck mockTruck3;
    private String mockAddress;
    private String[] mockList;

    public DataManagerMock(){
        super();

        mockList= new String[]{"AZERTY", "TEST123"};
        mockAddress="00:12:6F:35:7E:70";
        mockTruck1=new DataTruck();
        mockTruck1.setRegistrationID("AZERTY");
        mockTruck2=new DataTruck();
        mockTruck2.setRegistrationID("TEST123");
        mockTruck2.setA11(1);
        mockTruck2.setA12(2);
        mockTruck2.setA13(3);
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
    public void saveTruck(DataTruck truck2Save){ //simulate record in database
        mockTruck3=truck2Save;
        mockList[3]=truck2Save.getRegistrationID();
    }
    public void fetchMACAddrBT(){
        MACAddrBT=mockAddress;
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
        out.writeValue(mockTruck1);
        out.writeValue(mockTruck2);
        out.writeValue(mockTruck3);
        out.writeString(mockAddress);
        out.writeStringArray(mockList);
    }

    public static final Parcelable.Creator<DataManagerMock> CREATOR
            =new Parcelable.Creator<DataManagerMock>(){
        public DataManagerMock createFromParcel(Parcel in){
            return new DataManagerMock(in);
        }
        public DataManagerMock[] newArray(int size){
            return new DataManagerMock[size];
        }
    };
    private DataManagerMock(Parcel in){
        //ajouter attributs ici aussi
        in.writeStringArray(truckList);
        in.writeValue(selectedTruck);
        in.writeString(MACAddrBT);
        in.writeInt(targetSlump);
        in.writeValue(mockTruck1);
        in.writeValue(mockTruck2);
        in.writeValue(mockTruck3);
        in.writeString(mockAddress);
        in.writeStringArray(mockList);
    }

}
