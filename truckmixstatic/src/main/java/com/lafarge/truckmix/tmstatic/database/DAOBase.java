package com.lafarge.truckmix.tmstatic.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Kim.Abdoul-Carime on 28/08/2015.
 */
public abstract class DAOBase {

    protected final static int VERSION=1;
    protected final static String NOM ="TMstaticDB.db";
    protected SQLiteDatabase mDb =null;
    protected DataBaseHandler mHandler =null;
    public DAOBase(Context pContext){
        this.mHandler = new DataBaseHandler(pContext, NOM, null, VERSION);
    }
    public SQLiteDatabase open(){
        mDb=mHandler.getWritableDatabase();
        return mDb;
    }
    public void close(){
        mDb.close();
    }
    public SQLiteDatabase getDb(){
        return mDb;
    }
}
