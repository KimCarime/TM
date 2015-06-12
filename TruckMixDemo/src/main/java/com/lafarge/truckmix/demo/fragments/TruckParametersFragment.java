package com.lafarge.truckmix.demo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.demo.R;
import com.lafarge.truckmix.demo.utils.UserPreferences;

public class TruckParametersFragment extends Fragment {

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

    private UserPreferences mPrefs;

    public static TruckParametersFragment newInstance() {
        TruckParametersFragment fragment = new TruckParametersFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TruckParametersFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = new UserPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_truck_parameters, container, false);
        ButterKnife.inject(this, view);

        updateParameters();
        return view;
    }

    public void updateParameters() {
        TruckParameters parameters = mPrefs.getTruckParameters();
        mT1.setText(String.format("%f", parameters.T1));
        mA11.setText(String.format("%f", parameters.A11));
        mA12.setText(String.format("%f", parameters.A12));
        mA13.setText(String.format("%f", parameters.A13));
        mMagnetQuantity.setText(String.format("%d", parameters.magnetQuantity));
        mTimePump.setText(String.format("%d", parameters.timePump));
        mTimeDelayDriver.setText(String.format("%d", parameters.timeDelayDriver));
        mPulseNumber.setText(String.format("%d", parameters.pulseNumber));
        mFlowmeterFrequency.setText(String.format("%d", parameters.flowmeterFrequency));
        mCommandPumpMode.setSelection(((ArrayAdapter<String>) mCommandPumpMode.getAdapter()).getPosition
                (String.valueOf(parameters
                        .commandPumpMode)));
        mCalibrationInputSensorA.setText(String.format("%f", parameters.calibrationInputSensorA));
        mCalibrationInputSensorB.setText(String.format("%f", parameters.calibrationInputSensorB));
        mCalibrationOutputSensorA.setText(String.format("%f", parameters.calibrationOutputSensorA));
        mCalibrationOutputSensorB.setText(String.format("%f", parameters.calibrationOutputSensorB));
        mEV1.setText(String.format("%d", parameters.openingTimeEV1));
        mVA1.setText(String.format("%d", parameters.openingTimeVA1));
        mToleranceCounting.setText(String.format("%d", parameters.toleranceCounting));
        mWaitingDurationAfterWaterAddition.setText(String.format("%d", parameters.waitingDurationAfterWaterAddition));
        mMaxDelayBeforeFlowage.setText(String.format("%d", parameters.maxDelayBeforeFlowage));
        mMaxFlowageError.setText(String.format("%d", parameters.maxFlowageError));
        mMaxCoutingError.setText(String.format("%d", parameters.maxCountingError));
    }

    public TruckParameters getUpdatedParameters() {
        double T1 = Double.parseDouble(mT1.getText().toString().replace(",","."));
        double A11 = Double.parseDouble(mA11.getText().toString().replace(",", "."));
        double A12 = Double.parseDouble(mA12.getText().toString().replace(",", "."));
        double A13 = Double.parseDouble(mA13.getText().toString().replace(",", "."));
        int magnetQuantity = Integer.parseInt(mMagnetQuantity.getText().toString());
        int timePump = Integer.parseInt(mTimePump.getText().toString());
        int timeDelayDriver = Integer.parseInt(mTimeDelayDriver.getText().toString());
        int pulseNumber = Integer.parseInt(mPulseNumber.getText().toString());
        int flowmeterFrequency = Integer.parseInt(mFlowmeterFrequency.getText().toString());
        TruckParameters.CommandPumpMode commandPumpMode = TruckParameters.CommandPumpMode.valueOf(mCommandPumpMode
                .getSelectedItem().toString());
        double calibrationInputSensorA = Double.parseDouble(mCalibrationInputSensorA.getText().toString().replace(",", "."));
        double calibrationInputSensorB = Double.parseDouble(mCalibrationInputSensorB.getText().toString().replace(",", "."));
        double calibrationOutputSensorA = Double.parseDouble(mCalibrationOutputSensorA.getText().toString().replace(",", "."));
        double calibrationOutputSensorB = Double.parseDouble(mCalibrationOutputSensorB.getText().toString().replace(",", "."));
        int openingTimeEV1 = Integer.parseInt(mEV1.getText().toString());
        int openingTimeVA1 = Integer.parseInt(mVA1.getText().toString());
        int toleranceCounting = Integer.parseInt(mToleranceCounting.getText().toString());
        int waitingDurationAfterWaterAddition = Integer.parseInt(mWaitingDurationAfterWaterAddition.getText().toString());
        int maxDelayBeforeFlowage = Integer.parseInt(mMaxDelayBeforeFlowage.getText().toString());
        int maxFlowageError = Integer.parseInt(mMaxFlowageError.getText().toString());
        int maxCountingError = Integer.parseInt(mMaxCoutingError.getText().toString());

        return new TruckParameters(T1, A11, A12, A13, magnetQuantity, timePump, timeDelayDriver, pulseNumber,
                flowmeterFrequency, commandPumpMode, calibrationInputSensorA, calibrationInputSensorB, calibrationOutputSensorA, calibrationOutputSensorB, openingTimeEV1, openingTimeVA1, toleranceCounting, waitingDurationAfterWaterAddition, maxDelayBeforeFlowage, maxFlowageError, maxCountingError);
    }
}
