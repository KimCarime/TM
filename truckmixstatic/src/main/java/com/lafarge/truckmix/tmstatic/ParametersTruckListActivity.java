package com.lafarge.truckmix.tmstatic;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class ParametersTruckListActivity extends AppCompatActivity {

private ListView mTruckList=null; //list declaration
    private String[] mockedTrucks ={"AZERTY","PKNBR5"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters_truck_list);
        mTruckList= (ListView) findViewById(R.id.listTruck); // link list to the layout

        mTruckList.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,mockedTrucks)); //list creation
        mTruckList.setItemChecked(0,true); // select the first item -> no case with null selection
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
            startActivity(new Intent(ParametersTruckListActivity.this, ParametersTruckDetailsActivity.class));
            return true;
        }
        if (id == R.id.TruckListParam2) { // Edit truck
            startActivity(new Intent(ParametersTruckListActivity.this, ParametersTruckDetailsActivity.class));
            return true;
        }
        if (id == R.id.TruckListParam3) { // Delete truck
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
