package com.lafarge.truckmix.tmstatic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.lafarge.truckmix.tmstatic.database.DAOTrucks;
import com.lafarge.truckmix.tmstatic.utils.DataManagerMock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ParametersTruckListActivity extends AppCompatActivity {

    private ListView mTruckList=null; //list declaration
    //private String[] mockedTrucks ={"AZERTY","PKNBR5"};
    private DataManagerMock mDataManager;
    private ArrayAdapter<String> listAdapter=null;
    //Database
    private DAOTrucks db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters_truck_list);
        mTruckList= (ListView) findViewById(R.id.listTruck); // link list to the layout
        db=new DAOTrucks(this);

        //Data manager creation
        mDataManager= new DataManagerMock();
        //Fetch truck list in the database
        mDataManager.fetchTruckList(db.fetchTruckList());

        refreshList();
        mTruckList.setAdapter(listAdapter);
        mTruckList.setItemChecked(0, true); // select the first item -> no case with null selection
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_parameters_truck_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.TruckListParam1) { // New truck

            mDataManager.newTruck();
            Intent editingTruck=new Intent(ParametersTruckListActivity.this,ParametersTruckDetailsActivity.class);
            editingTruck.putExtra("new",mDataManager);
            startActivity(editingTruck);
            return true;
        }
        if (id == R.id.TruckListParam2) { // Edit truck
            mDataManager.fetchSelectedTruck(db.fetchTruck(listAdapter.getItem(mTruckList.getCheckedItemPosition())));
            Intent editingTruck=new Intent(ParametersTruckListActivity.this,ParametersTruckDetailsActivity.class);
            editingTruck.putExtra("edit",mDataManager);
            startActivity(editingTruck);
            return true;
        }
        if (id == R.id.TruckListParam3) { // Delete truck
            mDataManager.deleteTruck(listAdapter.getItem(mTruckList.getCheckedItemPosition()));
            Toast.makeText(ParametersTruckListActivity.this, getResources().getString(R.string.ParametersTruckList_deleteTruck)+listAdapter.getItem(mTruckList.getCheckedItemPosition())+"...", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //other methods
    private void refreshList(){
        List<String> buffer = new ArrayList<String>();
        mDataManager.fetchTruckList(db.fetchTruckList());
        if(mDataManager.getTruckList()==null)
            buffer.add(getResources().getString(R.string.noTruckAvailable));
        else {
            buffer=new ArrayList<String>(mDataManager.getTruckList());
        }
       listAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,buffer); //adapter refresh

    }
}
