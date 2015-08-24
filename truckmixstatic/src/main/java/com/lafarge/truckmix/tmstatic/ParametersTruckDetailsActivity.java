package com.lafarge.truckmix.tmstatic;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.lafarge.truckmix.common.enums.CommandPumpMode;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.tmstatic.utils.DataManagerMock;
import com.lafarge.truckmix.tmstatic.utils.DataTruck;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ParametersTruckDetailsActivity extends AppCompatActivity {

    @InjectView(R.id.RegistrationNumber) EditText mRegistrationNumber;
    @InjectView(R.id.T1) EditText mT1;
    @InjectView(R.id.A11) EditText mA11;
    @InjectView(R.id.A12) EditText mA12;
    @InjectView(R.id.A13) EditText mA13;
    @InjectView(R.id.magnetQuantity) EditText mMagnetQuantity;
    @InjectView(R.id.timePump) EditText mTimePump;
    @InjectView(R.id.timeDelayDriver) EditText mTimeDelayDriver;
    @InjectView(R.id.pulseNumber) EditText mPulseNumber;
    @InjectView(R.id.flowmeterFrequency) EditText mFlowmeterFrequency;
    @InjectView(R.id.commandPumpMode) Spinner mCommandPumpMode;
    @InjectView(R.id.inputPressureSensorA) EditText mCalibrationInputSensorA;
    @InjectView(R.id.inputPressureSensorB) EditText mCalibrationInputSensorB;
    @InjectView(R.id.outputPressureSensorA) EditText mCalibrationOutputSensorA;
    @InjectView(R.id.outputPressureSensorB) EditText mCalibrationOutputSensorB;
    @InjectView(R.id.EV1) EditText mEV1;
    @InjectView(R.id.VA1) EditText mVA1;
    @InjectView(R.id.toleranceCounting) EditText mToleranceCounting;
    @InjectView(R.id.delayAfterWaterAddition) EditText mWaitingDurationAfterWaterAddition;
    @InjectView(R.id.delayMaxBeforeFlowage) EditText mMaxDelayBeforeFlowage;
    @InjectView(R.id.maxErrorFlowage) EditText mMaxFlowageError;
    @InjectView(R.id.maxErrorCouting) EditText mMaxCoutingError;

    //attributes
    private DataManagerMock mDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters_truck_details);
        ButterKnife.inject(this);
        //get data
        Intent incomingIntent=getIntent();
        mDataManager =(DataManagerMock) incomingIntent.getSerializableExtra("new");
       if( (mDataManager)==null)
        {
            mDataManager =(DataManagerMock) incomingIntent.getSerializableExtra("edit");
            mRegistrationNumber.setEnabled(false);
        }
        resetParameters();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_parameters_truck_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.TruckDetailsParam1) { //save
            saveParameters();
            return true;
        }
        if (id == R.id.TruckDetailsParam2) { //reset
            resetParameters();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void resetParameters() {
        TruckParameters param=mDataManager.getSelectedTruck().getTruckParameters();
        mRegistrationNumber.setText("");
        mRegistrationNumber.setHint(mDataManager.getSelectedTruck().getRegistrationID());
        mT1.setText(String.format("%f", param.T1));
        mA11.setText(String.format("%f", param.A11));
        mA12.setText(String.format("%f", param.A12));
        mA13.setText(String.format("%f", param.A13));
        mMagnetQuantity.setText(String.format("%d",param.magnetQuantity));
        mTimePump.setText(String.format("%d",param.timePump));
        mTimeDelayDriver.setText(String.format("%d", param.timeDelayDriver));
        mPulseNumber.setText(String.format("%d", param.pulseNumber));
        mFlowmeterFrequency.setText(String.format("%d", param.flowmeterFrequency));
        mCommandPumpMode.setSelection(((ArrayAdapter<String>) mCommandPumpMode.getAdapter()).getPosition
                (String.valueOf(param.commandPumpMode)));
        mCalibrationInputSensorA.setText(String.format("%f", param.calibrationInputSensorA));
        mCalibrationInputSensorB.setText(String.format("%f", param.calibrationInputSensorB));
        mCalibrationOutputSensorA.setText(String.format("%f", param.calibrationOutputSensorA));
        mCalibrationOutputSensorB.setText(String.format("%f", param.calibrationOutputSensorB));
        mEV1.setText(String.format("%d", param.openingTimeEV1));
        mVA1.setText(String.format("%d", param.openingTimeVA1));
        mToleranceCounting.setText(String.format("%d", param.toleranceCounting));
        mWaitingDurationAfterWaterAddition.setText(String.format("%d", param.waitingDurationAfterWaterAddition));
        mMaxDelayBeforeFlowage.setText(String.format("%d", param.maxDelayBeforeFlowage));
        mMaxFlowageError.setText(String.format("%d", param.maxFlowageError));
        mMaxCoutingError.setText(String.format("%d", param.maxCountingError));

    }
    private void saveParameters(){
        mDataManager.getSelectedTruck().setRegistrationID(mRegistrationNumber.getText().toString());
        TruckParameters param=new TruckParameters(
        Double.parseDouble(mT1.getText().toString().replace(",", ".")),
        Double.parseDouble(mA11.getText().toString().replace(",", ".")),
        Double.parseDouble(mA12.getText().toString().replace(",", ".")),
        Double.parseDouble(mA13.getText().toString().replace(",", ".")),
        Integer.parseInt(mMagnetQuantity.getText().toString()),
        Integer.parseInt(mTimePump.getText().toString()),
        Integer.parseInt(mTimeDelayDriver.getText().toString()),
        Integer.parseInt(mPulseNumber.getText().toString()),
        Integer.parseInt(mFlowmeterFrequency.getText().toString()),
        CommandPumpMode.valueOf(mCommandPumpMode.getSelectedItem().toString()),
        Double.parseDouble(mCalibrationInputSensorA.getText().toString().replace(",", ".")),
        Double.parseDouble(mCalibrationInputSensorB.getText().toString().replace(",", ".")),
        Double.parseDouble(mCalibrationOutputSensorA.getText().toString().replace(",", ".")),
        Double.parseDouble(mCalibrationOutputSensorB.getText().toString().replace(",", ".")),
        Integer.parseInt(mEV1.getText().toString()),
        Integer.parseInt(mVA1.getText().toString()),
        Integer.parseInt(mToleranceCounting.getText().toString()),
        Integer.parseInt(mWaitingDurationAfterWaterAddition.getText().toString()),
        Integer.parseInt(mMaxDelayBeforeFlowage.getText().toString()),
        Integer.parseInt(mMaxFlowageError.getText().toString()),
        Integer.parseInt(mMaxCoutingError.getText().toString())
        );

        mDataManager.getSelectedTruck().setTruckParameters(param);
        mDataManager.saveTruck();

    }
}
