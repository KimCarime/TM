package com.lafarge.truckmix.tmstatic;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;
//view
import com.lafarge.truckmix.tmstatic.utils.DataManager;
import com.lafarge.truckmix.tmstatic.utils.DataManagerMock;
import com.lafarge.truckmix.tmstatic.utils.DataTruck;
import com.lafarge.truckmix.common.models.TruckParameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;



public class MainActivity extends AppCompatActivity {

    //butter knife objects
    @InjectView(R.id.buttonStartCalculation) Button mButtonStartCalculation;
    @InjectView(R.id.fieldTagetSlump) NumberPicker mTargetSlump;
    @InjectView(R.id.truckSelectionTruckID) Spinner Liste;
    @InjectView(R.id.fieldLoadVolume) NumberPicker mLoadVolume;


    private ArrayAdapter<String> spinnerAdapter=null;
    //Attributes
    private DataManagerMock mDataManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        //Data manager creation
        mDataManager= new DataManagerMock();
    //WIDGET
       //spinner
        refreshSpinner();
        Liste.setAdapter(spinnerAdapter);
        // number picker
        mLoadVolume.setMinValue(0);
        mLoadVolume.setMaxValue(15);
        mLoadVolume.setValue(6);

        mTargetSlump.setMinValue(0);
        mTargetSlump.setMaxValue(300);
        mTargetSlump.setValue(0);



        mButtonStartCalculation.setOnClickListener(StartCalculation); //listener creation for button start calculation
    }

    // Action
    private View.OnClickListener StartCalculation= new View.OnClickListener() //event handler for action start calculation
    {
        @Override
        public void onClick(View vue)
        {
            //get the field content and put them in the the data manager class
                //TruckID
            String _TruckID=Liste.getSelectedItem().toString();

            if (_TruckID==getResources().getString(R.string.noTruckAvailable)) //no truck available
            {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.noTruckAvailableWarning), Toast.LENGTH_SHORT).show();
            }
            else { //Truck is selected

                mDataManager.setVolumeLoad(String.valueOf(mLoadVolume.getValue())); //get the load volume
                mDataManager.fetchSelectedTruck(_TruckID);
                mDataManager.fetchMACAddrBT();// fetch mac address here to avoid data corruption when updating mac address on settings
                mDataManager.setTargetSlump(String.valueOf(mTargetSlump.getValue()));
                //TargetSlump
                Intent intentSlumpCalculation = new Intent(MainActivity.this, SlumpCalculationActivity.class);
                intentSlumpCalculation.putExtra("data", mDataManager);
                startActivity(intentSlumpCalculation);
                finish();
            }
        }
    };

// menu management
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //pass data throught intent
        Intent intentTruckSettings = new Intent(MainActivity.this,ParametersTruckListActivity.class);
        intentTruckSettings.putExtra("data",mDataManager);
        Intent intentCalculatorSettings = new Intent(MainActivity.this,ParametersCalculatorActivity.class);
        //intentCalculatorSettings.putExtra("data",mDataManager);

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_1) { // Truck settings
            startActivity(intentTruckSettings);
            return true;
        }
        if (id == R.id.menu_2) { // Calculator settings
            startActivity(intentCalculatorSettings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //other methods
    private void refreshSpinner()
    {

        List<String> buffer = new ArrayList<String>();
        mDataManager.fetchTruckList(); //fetch truck list in database
        if(mDataManager.getTruckList()==null)
            buffer.add(getResources().getString(R.string.noTruckAvailable));
        else {

            buffer=new ArrayList<String>(mDataManager.getTruckList());
        }
        //updating adapter
        spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, buffer);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_drop);
    }
}
