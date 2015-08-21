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
import com.lafarge.truckmix.tmstatic.utils.DataManagerMock;

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
        mRegistrationNumber.setText("");
        mRegistrationNumber.setHint(mDataManager.getSelectedTruck().getRegistrationID());
        mT1.setText(String.format("%f", mDataManager.getSelectedTruck().getT1()));
        mA11.setText(String.format("%f", mDataManager.getSelectedTruck().getA11()));
        mA12.setText(String.format("%f", mDataManager.getSelectedTruck().getA12()));
        mA13.setText(String.format("%f", mDataManager.getSelectedTruck().getA13()));
        mMagnetQuantity.setText(String.format("%d", mDataManager.getSelectedTruck().getMagnetQuantity()));
        mTimePump.setText(String.format("%d",mDataManager.getSelectedTruck().getTimePump()));
        mTimeDelayDriver.setText(String.format("%d", mDataManager.getSelectedTruck().getTimeDelayDriver()));
        mPulseNumber.setText(String.format("%d", mDataManager.getSelectedTruck().getPulseNumber()));
        mFlowmeterFrequency.setText(String.format("%d", mDataManager.getSelectedTruck().getFlowmeterFrequency()));
        mCommandPumpMode.setSelection(((ArrayAdapter<String>) mCommandPumpMode.getAdapter()).getPosition
                (String.valueOf(mDataManager.getSelectedTruck().getCommandPumpMode())));
        mCalibrationInputSensorA.setText(String.format("%f", mDataManager.getSelectedTruck().getCalibrationInputSensorA()));
        mCalibrationInputSensorB.setText(String.format("%f", mDataManager.getSelectedTruck().getCalibrationInputSensorB()));
        mCalibrationOutputSensorA.setText(String.format("%f", mDataManager.getSelectedTruck().getCalibrationOutputSensorA()));
        mCalibrationOutputSensorB.setText(String.format("%f", mDataManager.getSelectedTruck().getCalibrationOutputSensorB()));
        mEV1.setText(String.format("%d", mDataManager.getSelectedTruck().getOpeningTimeEV1()));
        mVA1.setText(String.format("%d", mDataManager.getSelectedTruck().getOpeningTimeVA1()));
        mToleranceCounting.setText(String.format("%d", mDataManager.getSelectedTruck().getToleranceCounting()));
        mWaitingDurationAfterWaterAddition.setText(String.format("%d", mDataManager.getSelectedTruck().getWaitingDurationAfterWaterAddition()));
        mMaxDelayBeforeFlowage.setText(String.format("%d", mDataManager.getSelectedTruck().getMaxDelayBeforeFlowage()));
        mMaxFlowageError.setText(String.format("%d", mDataManager.getSelectedTruck().getMaxFlowageError()));
        mMaxCoutingError.setText(String.format("%d", mDataManager.getSelectedTruck().getMaxCountingError()));

    }
    private void saveParameters(){
        mDataManager.getSelectedTruck().setT1(Double.parseDouble(mT1.getText().toString().replace(",", ".")));
        mDataManager.getSelectedTruck().setA11(Double.parseDouble(mA11.getText().toString().replace(",", ".")));
        mDataManager.getSelectedTruck().setA12(Double.parseDouble(mA12.getText().toString().replace(",", ".")));
        mDataManager.getSelectedTruck().setA13(Double.parseDouble(mA13.getText().toString().replace(",", ".")));
        mDataManager.getSelectedTruck().setMagnetQuantity(Integer.parseInt(mMagnetQuantity.getText().toString()));
        mDataManager.getSelectedTruck().setTimePump(Integer.parseInt(mTimePump.getText().toString()));
        mDataManager.getSelectedTruck().setTimeDelayDriver(Integer.parseInt(mTimeDelayDriver.getText().toString()));
        mDataManager.getSelectedTruck().setPulseNumber(Integer.parseInt(mPulseNumber.getText().toString()));
        mDataManager.getSelectedTruck().setFlowmeterFrequency(Integer.parseInt(mFlowmeterFrequency.getText().toString()));
        mDataManager.getSelectedTruck().setCommandPumpMode(CommandPumpMode.valueOf(mCommandPumpMode.getSelectedItem().toString()));
        mDataManager.getSelectedTruck().setCalibrationInputSensorA(Double.parseDouble(mCalibrationInputSensorA.getText().toString().replace(",", ".")));
        mDataManager.getSelectedTruck().setCalibrationInputSensorB(Double.parseDouble(mCalibrationInputSensorB.getText().toString().replace(",", ".")));
        mDataManager.getSelectedTruck().setCalibrationOutputSensorA(Double.parseDouble(mCalibrationOutputSensorA.getText().toString().replace(",", ".")));
        mDataManager.getSelectedTruck().setCalibrationOutputSensorB(Double.parseDouble(mCalibrationOutputSensorB.getText().toString().replace(",", ".")));
        mDataManager.getSelectedTruck().setOpeningTimeEV1(Integer.parseInt(mEV1.getText().toString()));
        mDataManager.getSelectedTruck().setOpeningTimeVA1(Integer.parseInt(mVA1.getText().toString()));
        mDataManager.getSelectedTruck().setToleranceCounting(Integer.parseInt(mToleranceCounting.getText().toString()));
        mDataManager.getSelectedTruck().setWaitingDurationAfterWaterAddition(Integer.parseInt(mWaitingDurationAfterWaterAddition.getText().toString()));
        mDataManager.getSelectedTruck().setMaxDelayBeforeFlowage(Integer.parseInt(mMaxDelayBeforeFlowage.getText().toString()));
        mDataManager.getSelectedTruck().setMaxFlowageError(Integer.parseInt(mMaxFlowageError.getText().toString()));
        mDataManager.getSelectedTruck().setMaxCountingError(Integer.parseInt(mMaxCoutingError.getText().toString()));

        mDataManager.saveTruck();

    }
}
