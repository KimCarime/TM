package com.lafarge.truckmix.tmstatic;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
//view
import com.lafarge.truckmix.tmstatic.utils.DataManager;
import com.lafarge.truckmix.tmstatic.utils.DataManagerMock;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity {

    //butter knife objects
    @InjectView(R.id.buttonStartCalculation) Button mButtonStartCalculation;
    @InjectView(R.id.fieldTruckID) EditText mTruckID;
    @InjectView(R.id.fieldTagetSlump) EditText mTargetSlump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        //Data manager creation
        DataManagerMock mDataManager= new DataManagerMock();
       //Fetch truck list in the database
        mDataManager.fetchTruckList();

        mButtonStartCalculation.setOnClickListener(StartCalculation); //listener creation for button start calculation
    }

    // Action
    private View.OnClickListener StartCalculation= new View.OnClickListener() //event handler for action start calculation
    {
        @Override
        public void onClick(View vue)
        {
            startActivity(new Intent(MainActivity.this, SlumpCalculationActivity.class));
            finish();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_1) { // Truck settings
            startActivity(new Intent(MainActivity.this, ParametersTruckListActivity.class));
            return true;
        }
        if (id == R.id.menu_2) { // Calculator settings
            startActivity(new Intent(MainActivity.this, ParametersCalculatorActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
