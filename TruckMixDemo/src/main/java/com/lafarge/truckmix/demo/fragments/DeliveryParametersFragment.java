package com.lafarge.truckmix.demo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.demo.R;
import com.lafarge.truckmix.demo.utils.UserPreferences;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DeliveryParametersFragment extends Fragment {

    @InjectView(R.id.targetSlump) EditText mTargetSlump;
    @InjectView(R.id.maxWater) EditText mMaxWater;
    @InjectView(R.id.loadVolume) EditText mLoadVolume;

    UserPreferences mPrefs;

    //
    // Constructor
    //

    public static DeliveryParametersFragment newInstance() {
        return new DeliveryParametersFragment();
    }

    public DeliveryParametersFragment() {
        // Required empty public constructor
    }

    //
    // View lifecycle
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = new UserPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_delivery_parameters, container, false);
        ButterKnife.inject(this, view);

        updateParameters();
        return view;
    }

    //
    // Public
    //

    public void updateParameters() {
        DeliveryParameters paramers = mPrefs.getDeliveryParameters();
        mTargetSlump.setText(String.format("%d", paramers.targetSlump));
        mMaxWater.setText(String.format("%d", paramers.maxWater));
        mLoadVolume.setText(String.format("%d", paramers.loadVolume));
    }

    public DeliveryParameters getUpdatedParameters() {
        int targetSlump = Integer.parseInt(mTargetSlump.getText().toString());
        int maxWater = Integer.parseInt(mTargetSlump.getText().toString());
        int loadVolume = Integer.parseInt(mTargetSlump.getText().toString());

        return new DeliveryParameters(targetSlump, maxWater, loadVolume);
    }
}
