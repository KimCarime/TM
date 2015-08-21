package com.lafarge.truckmix.tmstatic;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lafarge.truckmix.tmstatic.utils.DataManager;
import com.lafarge.truckmix.tmstatic.utils.DataManagerMock;
import com.lafarge.truckmix.tmstatic.utils.DataTruck;

import javax.xml.datatype.DatatypeConfigurationException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class SlumpCalculationActivity extends AppCompatActivity {

    //butter knife objects
    @InjectView(R.id.slumpCalculationMeasuredSlump) TextView mTextViewMeasuredSlump;
    @InjectView(R.id.slumpCalculationTargetSlump) TextView mTextViewTargetSlump;
    @InjectView(R.id.slumpCalculationTuckID) TextView mTextViewTruckID;
    @InjectView(R.id.slumpCalculationEndCalculation) Button mButtonEndCalculation;
    //attributes
    private DataManagerMock mDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slump_calculation);
        ButterKnife.inject(this);

        //get data
        Intent incomingIntent=getIntent();
        mDataManager =(DataManagerMock) incomingIntent.getSerializableExtra("data");


        //widget initialisation
        mButtonEndCalculation.setOnClickListener(EndCalculation); // Event listener
        mTextViewTruckID.setText(mDataManager.getSelectedTruck().getRegistrationID());
        mTextViewTargetSlump.setText( mDataManager.getTargetSlump());

    }

    //Action management
    private View.OnClickListener EndCalculation= new View.OnClickListener(){ //event handler
        @Override
        public void onClick(View vue)
        {
            startActivity(new Intent(SlumpCalculationActivity.this, MainActivity.class));
            finish();
        }
    };

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_slump_calculation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
